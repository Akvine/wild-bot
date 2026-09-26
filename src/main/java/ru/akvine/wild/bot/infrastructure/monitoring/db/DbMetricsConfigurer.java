package ru.akvine.wild.bot.infrastructure.monitoring.db;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.MissConfigurationException;

/**
 * Собирает {@link DbMetrics} из настроек {@code db.metrics.*}. Ключи передаются без префикса:
 * <pre>
 * enabled                                  - включить все метрики
 * commit.log.enabled                        - писать каждый коммит в лог
 * commit.stacktrace.log.enabled             - писать с коммитом стек вызовов
 * jmx.commit.metric.enabled / .name         - общий счётчик коммитов в JMX и его имя
 * thread.metrics                           - мнемоники групп потоков через запятую
 * &lt;поток&gt;.thread.name.regex                - регулярное выражение от имени потока (обязательно)
 * &lt;поток&gt;.enabled, &lt;поток&gt;.commit.log.enabled, &lt;поток&gt;.commit.stacktrace.log.enabled
 * &lt;поток&gt;.query.metrics                     - мнемоники запросов группы через запятую
 * &lt;поток&gt;.&lt;запрос&gt;.regex                   - регулярное выражение от SQL (обязательно)
 * &lt;поток&gt;.&lt;запрос&gt;.enabled, &lt;поток&gt;.&lt;запрос&gt;.exclude.classes
 * </pre>
 */
@Slf4j
public final class DbMetricsConfigurer {
    private static final String PADDING = "\t";

    private DbMetricsConfigurer() {}

    /**
     * @param properties настройки {@code db.metrics.*} без префикса
     * @param jmxMetrics реестр JMX-метрик; нужен, только если включён счётчик коммитов в JMX
     */
    public static DbMetricsService configure(Map<String, String> properties, JmxMetrics jmxMetrics) {
        Objects.requireNonNull(properties, "properties must be present");

        DbMetrics metrics = new DbMetrics(loggingConfig(properties, null));
        DbMetricsService service = new DbMetricsService(metrics);

        if (properties.containsKey("enabled")) {
            metrics.setEnabled(Boolean.parseBoolean(properties.get("enabled")));
        }
        if (Boolean.parseBoolean(properties.get("jmx.commit.metric.enabled"))) {
            String name = properties.get("jmx.commit.metric.name");
            if (!StringUtils.hasText(name)) {
                throw new MissConfigurationException("jmx.commit.metric.name is blank");
            }
            if (jmxMetrics == null) {
                throw new MissConfigurationException("jmxMetrics is null");
            }
            metrics.createTotalCommitCountJmxMetric(jmxMetrics, name);
        }

        String threadMetrics = properties.get("thread.metrics");
        if (StringUtils.hasText(threadMetrics)) {
            split(threadMetrics).forEach(mnemonic -> configureThreadMetric(metrics, properties, mnemonic));
        }

        logger.debug("Configured db metrics: {}", service.metricsToPrintableString());
        return service;
    }

    private static void configureThreadMetric(DbMetrics metrics, Map<String, String> properties, String thread) {
        String threadNameRegex = properties.get(key(thread, "thread.name.regex"));
        if (!StringUtils.hasText(threadNameRegex)) {
            throw new MissConfigurationException(String.format("thread.name.regex for [%s] is blank", thread));
        }

        DbMetricPerThread threadMetric =
                new DbMetricPerThread(thread, threadNameRegex, loggingConfig(properties, thread));
        metrics.addOrReplaceThreadMetric(threadMetric);
        if (properties.containsKey(key(thread, "enabled"))) {
            threadMetric.setEnabled(Boolean.parseBoolean(properties.get(key(thread, "enabled"))));
        }

        String queryMetrics = properties.get(key(thread, "query.metrics"));
        if (StringUtils.hasText(queryMetrics)) {
            split(queryMetrics).forEach(query -> configureQueryMetric(threadMetric, properties, thread, query));
        }
    }

    private static void configureQueryMetric(
            DbMetricPerThread threadMetric, Map<String, String> properties, String thread, String query) {
        String queryRegex = properties.get(key(thread, query, "regex"));
        if (!StringUtils.hasText(queryRegex)) {
            throw new MissConfigurationException(String.format("regex for [%s].[%s] is blank", thread, query));
        }

        DbMetricPerQuery queryMetric = new DbMetricPerQuery(query, queryRegex);
        threadMetric.addOrReplaceQueryMetric(queryMetric);
        if (properties.containsKey(key(thread, query, "enabled"))) {
            queryMetric.setEnabled(Boolean.parseBoolean(properties.get(key(thread, query, "enabled"))));
        }
        queryMetric.parseAndAddExcludeClasses(properties.get(key(thread, query, "exclude.classes")));
    }

    private static DbMetricsLoggingConfig loggingConfig(Map<String, String> properties, String thread) {
        String commitLog = thread == null ? "commit.log.enabled" : key(thread, "commit.log.enabled");
        String commitStackTraceLog =
                thread == null ? "commit.stacktrace.log.enabled" : key(thread, "commit.stacktrace.log.enabled");

        DbMetricsLoggingConfig loggingConfig = new DbMetricsLoggingConfig();
        if (properties.containsKey(commitLog)) {
            loggingConfig.setCommitLoggingEnabled(Boolean.parseBoolean(properties.get(commitLog)));
        }
        if (properties.containsKey(commitStackTraceLog)) {
            loggingConfig.setCommitStackTraceLoggingEnabled(Boolean.parseBoolean(properties.get(commitStackTraceLog)));
        }
        return loggingConfig;
    }

    private static List<String> split(String commaSeparated) {
        return Arrays.asList(commaSeparated.replaceAll(" ", "").split(","));
    }

    private static String key(String... parts) {
        return String.join(".", parts);
    }

    public static String toPrintableString(DbMetrics metrics) {
        Objects.requireNonNull(metrics, "metrics must be present");
        StringBuilder sb = new StringBuilder();
        line(sb, "\n---------------------------------- DbMetrics -------------------------------------");
        line(sb, "enable = ", String.valueOf(metrics.getEnabled()));
        line(sb, "totalCommitCount = ", String.valueOf(metrics.getTotalCommitCount()));
        line(sb, "totalCommitCountDisplayedInJmx = ", String.valueOf(metrics.isTotalCommitCountDisplayedInJmx()));
        if (metrics.getJmxCommitCounter() != null) {
            line(sb, "jmxCommitCounter.name = ", metrics.getJmxCommitCounterName());
            line(
                    sb,
                    "jmxCommitCounter.value = ",
                    String.valueOf(metrics.getJmxCommitCounter().getCount()));
        } else {
            line(sb, "jmxCommitCounter NOT initialized");
        }
        loggingConfigToString(sb, metrics.getLoggingConfig(), "");
        List<DbMetricPerThread> threadMetrics = metrics.getThreadMetrics();
        if (!threadMetrics.isEmpty()) {
            line(sb, "ThreadMetrics:");
            threadMetrics.forEach(threadMetric -> threadMetricToString(sb, threadMetric));
        }
        line(sb, "----------------------------------------------------------------------------------");
        return sb.toString();
    }

    private static void loggingConfigToString(StringBuilder sb, DbMetricsLoggingConfig config, String padding) {
        line(sb, padding, "LoggingConfig:");
        line(sb, padding, PADDING, "commitLoggingEnabled = ", String.valueOf(config.isCommitLoggingEnabled()));
        line(
                sb,
                padding,
                PADDING,
                "commitStackTraceLoggingEnabled = ",
                String.valueOf(config.isCommitStackTraceLoggingEnabled()));
    }

    private static void threadMetricToString(StringBuilder sb, DbMetricPerThread threadMetric) {
        line(sb, PADDING, "- ", threadMetric.getMnemonic());
        line(sb, PADDING, PADDING, "enable = ", String.valueOf(threadMetric.isEnabled()));
        line(sb, PADDING, PADDING, "threadNameRegex = ", threadMetric.getThreadNameRegex());
        line(sb, PADDING, PADDING, "commitCount = ", String.valueOf(threadMetric.getCommitCount()));
        loggingConfigToString(sb, threadMetric.getLoggingConfig(), PADDING + PADDING);
        List<DbMetricPerQuery> queryMetrics = threadMetric.getQueryMetrics();
        if (!queryMetrics.isEmpty()) {
            line(sb, PADDING, PADDING, "QueriesMetrics:");
            queryMetrics.forEach(queryMetric -> {
                line(sb, PADDING, PADDING, PADDING, "- ", queryMetric.getMnemonic());
                String padding = PADDING + PADDING + PADDING + PADDING;
                line(sb, padding, "enable = ", String.valueOf(queryMetric.isEnabled()));
                line(sb, padding, "excludeClasses = ", String.join(", ", queryMetric.getExcludeClasses()));
                line(sb, padding, "queryRegex = ", queryMetric.getQueryRegex());
                line(sb, padding, "queriesCount = ", String.valueOf(queryMetric.getQueriesCount()));
            });
        }
    }

    private static void line(StringBuilder sb, String... parts) {
        Arrays.stream(parts).forEach(sb::append);
        sb.append("\n");
    }
}
