package ru.akvine.wild.bot.infrastructure.monitoring.db;

import com.codahale.metrics.Counter;
import java.util.List;
import java.util.Objects;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.QueryMetricNotFound;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.ThreadMetricNotFound;

/**
 * Управление {@link DbMetrics} во время работы приложения: включение и выключение метрик, чтение и
 * сброс счётчиков по мнемоникам (именам метрик из настроек {@code db.metrics.*}).
 */
public class DbMetricsService {
    private final DbMetrics metrics;

    public DbMetricsService(DbMetrics metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics must be present");
    }

    public DbMetrics getMetrics() {
        return metrics;
    }

    public DbMetricPerThread getThreadMetric(String threadMnemonic) {
        return metrics.getThreadMetric(threadMnemonic);
    }

    public List<DbMetricPerThread> getThreadMetrics() {
        return metrics.getThreadMetrics();
    }

    public DbMetricPerThread getRequiredThreadMetric(String threadMnemonic) {
        DbMetricPerThread threadMetric = getThreadMetric(threadMnemonic);
        if (threadMetric == null) {
            throw new ThreadMetricNotFound(threadMnemonic);
        }
        return threadMetric;
    }

    public DbMetricsLoggingConfig getLoggingConfig() {
        return metrics.getLoggingConfig();
    }

    public DbMetricsLoggingConfig getLoggingConfig(String threadMnemonic) {
        return getRequiredThreadMetric(threadMnemonic).getLoggingConfig();
    }

    public long getTotalCommitCount() {
        return metrics.getTotalCommitCount();
    }

    public long getCommitCount(String threadMnemonic) {
        return getRequiredThreadMetric(threadMnemonic).getCommitCount();
    }

    public void resetTotalCommitCount() {
        metrics.resetTotalCommitCount();
    }

    public void resetCommitCount(String threadMnemonic) {
        getRequiredThreadMetric(threadMnemonic).resetCommitCount();
    }

    public boolean isEnable() {
        return metrics.getEnabled();
    }

    public void setEnabled(boolean isEnable) {
        metrics.setEnabled(isEnable);
    }

    public boolean isTotalCommitCountDisplayedInJmx() {
        return metrics.isTotalCommitCountDisplayedInJmx();
    }

    public void disableTotalCommitCountDisplayedInJmx() {
        metrics.disableTotalCommitCountDisplayedInJmx();
    }

    public void enableTotalCommitCountDisplayedInJmx() {
        metrics.enableTotalCommitCountDisplayedInJmx();
    }

    public String getJmxTotalCommitMetricName() {
        return metrics.getJmxCommitCounterName();
    }

    public Counter getJmxTotalCommitMetric() {
        return metrics.getJmxCommitCounter();
    }

    public boolean isThreadMetricEnabled(String threadMnemonic) {
        return getRequiredThreadMetric(threadMnemonic).isEnabled();
    }

    public void setThreadMetricEnable(boolean isEnable, String threadMnemonic) {
        getRequiredThreadMetric(threadMnemonic).setEnabled(isEnable);
    }

    public String getThreadNameRegex(String threadMnemonic) {
        return getRequiredThreadMetric(threadMnemonic).getThreadNameRegex();
    }

    public List<DbMetricPerQuery> getQueryMetrics(String threadMnemonic) {
        return getRequiredThreadMetric(threadMnemonic).getQueryMetrics();
    }

    public DbMetricPerQuery getQueryMetric(String threadMnemonic, String queryMnemonic) {
        return getRequiredThreadMetric(threadMnemonic).getQueryMetric(queryMnemonic);
    }

    public DbMetricPerQuery getRequiredQueryMetric(String threadMnemonic, String queryMnemonic) {
        DbMetricPerQuery queryMetric = getRequiredThreadMetric(threadMnemonic).getQueryMetric(queryMnemonic);
        if (queryMetric == null) {
            throw new QueryMetricNotFound(threadMnemonic, queryMnemonic);
        }
        return queryMetric;
    }

    public boolean isQueryMetricEnabled(String threadMnemonic, String queryMnemonic) {
        return getRequiredQueryMetric(threadMnemonic, queryMnemonic).isEnabled();
    }

    public void setQueryMetricEnable(boolean isEnable, String threadMnemonic, String queryMnemonic) {
        getRequiredQueryMetric(threadMnemonic, queryMnemonic).setEnabled(isEnable);
    }

    public String getQueryRegex(String threadMnemonic, String queryMnemonic) {
        return getRequiredQueryMetric(threadMnemonic, queryMnemonic).getQueryRegex();
    }

    public long getQueryCount(String threadMnemonic, String queryMnemonic) {
        return getRequiredQueryMetric(threadMnemonic, queryMnemonic).getQueriesCount();
    }

    public void resetQueryCount(String threadMnemonic, String queryMnemonic) {
        getRequiredQueryMetric(threadMnemonic, queryMnemonic).resetQueriesCount();
    }

    public List<String> getExcludeClasses(String threadMnemonic, String queryMnemonic) {
        return getRequiredQueryMetric(threadMnemonic, queryMnemonic).getExcludeClasses();
    }

    public void parseAndAddExcludeClasses(String threadMnemonic, String queryMnemonic, String excludeClasses) {
        getRequiredQueryMetric(threadMnemonic, queryMnemonic).parseAndAddExcludeClasses(excludeClasses);
    }

    public void addExcludeClass(String threadMnemonic, String queryMnemonic, String excludeClass) {
        getRequiredQueryMetric(threadMnemonic, queryMnemonic).addExcludeClass(excludeClass);
    }

    public String metricsToPrintableString() {
        return DbMetricsConfigurer.toPrintableString(metrics);
    }
}
