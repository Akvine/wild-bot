package ru.akvine.wild.bot.infrastructure.monitoring.db;

import com.codahale.metrics.Counter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.JmxTotalCommitCounterNotInitialized;

/**
 * Корень метрик работы с БД: общее число коммитов (его можно вывести в JMX) и метрики по группам
 * потоков ({@link DbMetricPerThread}). Всё включается и выключается на лету.
 */
public class DbMetrics {
    private final AtomicLong totalCommitCount = new AtomicLong(0);
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final DbMetricsLoggingConfig loggingConfig;
    private final Map<String, DbMetricPerThread> threadMetrics = new ConcurrentHashMap<>();

    private volatile Counter jmxCommitCounter;
    private final AtomicReference<String> jmxCommitCounterName = new AtomicReference<>();
    private final AtomicBoolean totalCommitCountDisplayedInJmx = new AtomicBoolean(false);

    public DbMetrics(DbMetricsLoggingConfig loggingConfig) {
        this.loggingConfig = Objects.requireNonNull(loggingConfig, "loggingConfig must be present");
    }

    public DbMetricsLoggingConfig getLoggingConfig() {
        return loggingConfig;
    }

    public boolean getEnabled() {
        return enabled.get();
    }

    public DbMetrics setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }

    public DbMetrics disableTotalCommitCountDisplayedInJmx() {
        this.totalCommitCountDisplayedInJmx.set(false);
        return this;
    }

    public DbMetrics enableTotalCommitCountDisplayedInJmx() {
        if (jmxCommitCounter == null) {
            throw new JmxTotalCommitCounterNotInitialized();
        }
        this.totalCommitCountDisplayedInJmx.set(true);
        return this;
    }

    public DbMetrics createTotalCommitCountJmxMetric(JmxMetrics metrics, String name) {
        this.jmxCommitCounter = metrics.counter(name);
        this.jmxCommitCounterName.set(name);
        this.totalCommitCountDisplayedInJmx.set(true);
        return this;
    }

    public String getJmxCommitCounterName() {
        return this.jmxCommitCounterName.get();
    }

    public Counter getJmxCommitCounter() {
        return jmxCommitCounter;
    }

    public boolean isTotalCommitCountDisplayedInJmx() {
        return totalCommitCountDisplayedInJmx.get();
    }

    public void resetTotalCommitCount() {
        totalCommitCount.set(0);
        Counter counter = jmxCommitCounter;
        if (totalCommitCountDisplayedInJmx.get() && counter != null) {
            counter.dec(counter.getCount());
        }
    }

    public long getTotalCommitCount() {
        return totalCommitCount.get();
    }

    public void incrementTotalCommitCount() {
        totalCommitCount.incrementAndGet();
        Counter counter = jmxCommitCounter;
        if (totalCommitCountDisplayedInJmx.get() && counter != null) {
            counter.inc();
        }
    }

    public List<DbMetricPerThread> getThreadMetrics() {
        return new ArrayList<>(threadMetrics.values());
    }

    /**
     * @return метрика группы потоков или {@code null}, если такой нет
     */
    public DbMetricPerThread getThreadMetric(String threadMnemonic) {
        return threadMetrics.get(threadMnemonic);
    }

    public DbMetrics addOrReplaceThreadMetric(DbMetricPerThread threadMetric) {
        threadMetrics.put(threadMetric.getMnemonic(), threadMetric);
        return this;
    }

    public DbMetrics addOrReplaceThreadMetrics(List<DbMetricPerThread> threadMetrics) {
        threadMetrics.forEach(this::addOrReplaceThreadMetric);
        return this;
    }

    public DbMetrics removeThreadMetric(DbMetricPerThread threadMetric) {
        threadMetrics.remove(threadMetric.getMnemonic());
        return this;
    }

    public DbMetrics removeThreadMetric(String threadMnemonic) {
        threadMetrics.remove(threadMnemonic);
        return this;
    }
}
