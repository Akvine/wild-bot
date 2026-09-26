package ru.akvine.wild.bot.infrastructure.monitoring.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Метрики группы потоков, выбранных по регулярному выражению от имени потока (например,
 * {@code scheduling-.*} или {@code http-nio-.*}): число коммитов и набор {@link DbMetricPerQuery}.
 */
public class DbMetricPerThread {
    private final String mnemonic;
    private final String threadNameRegex;
    private final Pattern threadNamePattern;
    private final DbMetricsLoggingConfig loggingConfig;
    private final Map<String, DbMetricPerQuery> queryMetrics = new ConcurrentHashMap<>();
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicLong commitCount = new AtomicLong(0);

    public DbMetricPerThread(String mnemonic, String threadNameRegex, DbMetricsLoggingConfig loggingConfig) {
        this.mnemonic = Objects.requireNonNull(mnemonic, "mnemonic must be present");
        this.threadNameRegex = Objects.requireNonNull(threadNameRegex, "threadNameRegex must be present");
        this.loggingConfig = Objects.requireNonNull(loggingConfig, "loggingConfig must be present");
        this.threadNamePattern = Pattern.compile(threadNameRegex);
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public DbMetricsLoggingConfig getLoggingConfig() {
        return loggingConfig;
    }

    public void resetCommitCount() {
        commitCount.set(0);
    }

    public long getCommitCount() {
        return commitCount.get();
    }

    public void incrementCommitCount() {
        commitCount.incrementAndGet();
    }

    public String getThreadNameRegex() {
        return threadNameRegex;
    }

    public boolean matchesThread(String threadName) {
        return threadNamePattern.matcher(threadName).matches();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public DbMetricPerThread setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }

    public DbMetricPerThread addOrReplaceQueryMetrics(List<DbMetricPerQuery> queryMetrics) {
        queryMetrics.forEach(this::addOrReplaceQueryMetric);
        return this;
    }

    public DbMetricPerThread addOrReplaceQueryMetric(DbMetricPerQuery queryMetric) {
        queryMetrics.put(queryMetric.getMnemonic(), queryMetric);
        return this;
    }

    public DbMetricPerThread removeQueryMetric(DbMetricPerQuery queryMetric) {
        queryMetrics.remove(queryMetric.getMnemonic());
        return this;
    }

    public DbMetricPerThread removeQueryMetric(String queryMnemonic) {
        queryMetrics.remove(queryMnemonic);
        return this;
    }

    public List<DbMetricPerQuery> getQueryMetrics() {
        return new ArrayList<>(queryMetrics.values());
    }

    /**
     * @return метрика запроса или {@code null}, если такой нет
     */
    public DbMetricPerQuery getQueryMetric(String queryMnemonic) {
        return queryMetrics.get(queryMnemonic);
    }
}
