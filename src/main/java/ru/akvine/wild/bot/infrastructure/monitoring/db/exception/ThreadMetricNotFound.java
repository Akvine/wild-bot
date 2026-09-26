package ru.akvine.wild.bot.infrastructure.monitoring.db.exception;

public class ThreadMetricNotFound extends DbMetricsException {
    public ThreadMetricNotFound(String threadMnemonic) {
        super("Thread metric [" + threadMnemonic + "] not found");
    }
}
