package ru.akvine.wild.bot.infrastructure.monitoring.db;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import ru.akvine.wild.bot.infrastructure.monitoring.SqlExecutionListener;

/**
 * Обновляет {@link DbMetrics} по событиям JDBC-прокси: считает коммиты и запросы потоков, попавших
 * под настроенные метрики. При выключенных метриках ({@link DbMetrics#getEnabled()}) ничего не делает.
 * Коммиты (и при необходимости их стек вызовов) пишутся в отдельный логгер
 * {@value #LOGGER_NAME}.
 */
@Slf4j(topic = DbMetricsListener.LOGGER_NAME)
public class DbMetricsListener implements SqlExecutionListener {
    public static final String LOGGER_NAME = "ru.akvine.wild.bot.monitoring.DbMetrics";

    private final DbMetrics metrics;

    public DbMetricsListener(DbMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void onQueryExecuted(String sql, long startedAtNanos) {
        if (!metrics.getEnabled() || !StringUtils.hasText(sql)) {
            return;
        }
        for (DbMetricPerThread threadMetric : availableThreadMetrics()) {
            for (DbMetricPerQuery queryMetric : threadMetric.getQueryMetrics()) {
                if (queryMetric.isEnabled() && queryMetric.matches(sql) && !isExcluded(queryMetric)) {
                    queryMetric.incrementQueriesCount();
                }
            }
        }
    }

    @Override
    public void onCommit() {
        if (!metrics.getEnabled()) {
            return;
        }
        metrics.incrementTotalCommitCount();
        DbMetricsLoggingConfig globalLogging = metrics.getLoggingConfig();
        for (DbMetricPerThread threadMetric : availableThreadMetrics()) {
            DbMetricsLoggingConfig threadLogging = threadMetric.getLoggingConfig();
            if (globalLogging.isCommitStackTraceLoggingEnabled() && threadLogging.isCommitStackTraceLoggingEnabled()) {
                logger.info(
                        "Commit [threadMetric={}] StackTrace:\n{}",
                        threadMetric.getMnemonic(),
                        stream(Thread.currentThread().getStackTrace())
                                .map(Object::toString)
                                .collect(joining("\n")));
            } else if (globalLogging.isCommitLoggingEnabled() && threadLogging.isCommitLoggingEnabled()) {
                logger.info("Commit [threadMetric={}]", threadMetric.getMnemonic());
            }
            threadMetric.incrementCommitCount();
        }
    }

    private List<DbMetricPerThread> availableThreadMetrics() {
        String currentThreadName = Thread.currentThread().getName();
        return metrics.getThreadMetrics().stream()
                .filter(threadMetric -> threadMetric.isEnabled() && threadMetric.matchesThread(currentThreadName))
                .toList();
    }

    private boolean isExcluded(DbMetricPerQuery queryMetric) {
        List<String> excludeClasses = queryMetric.getExcludeClasses();
        if (excludeClasses.isEmpty()) {
            return false;
        }
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            for (String excludeClass : excludeClasses) {
                if (element.getClassName().contains(excludeClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
