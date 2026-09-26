package ru.akvine.wild.bot.infrastructure.monitoring.db;

import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * Реестр Codahale-метрик, публикуемых в JMX в заданном домене
 */
public class JmxMetrics {
    private final MetricRegistry registry = new MetricRegistry();
    private final JmxReporter reporter;

    public JmxMetrics(String domain) {
        this.reporter = JmxReporter.forRegistry(registry).inDomain(domain).build();
    }

    public Counter counter(String name) {
        return registry.counter(name);
    }

    public void start() {
        reporter.start();
    }

    public void stop() {
        reporter.stop();
    }
}
