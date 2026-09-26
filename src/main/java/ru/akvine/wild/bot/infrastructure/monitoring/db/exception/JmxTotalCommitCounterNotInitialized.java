package ru.akvine.wild.bot.infrastructure.monitoring.db.exception;

public class JmxTotalCommitCounterNotInitialized extends DbMetricsException {
    public JmxTotalCommitCounterNotInitialized() {
        super("JMX total commit counter is not initialized: create it first with createTotalCommitCountJmxMetric");
    }
}
