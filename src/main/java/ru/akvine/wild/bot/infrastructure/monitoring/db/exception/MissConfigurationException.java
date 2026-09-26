package ru.akvine.wild.bot.infrastructure.monitoring.db.exception;

public class MissConfigurationException extends DbMetricsException {
    public MissConfigurationException(String message) {
        super(message);
    }
}
