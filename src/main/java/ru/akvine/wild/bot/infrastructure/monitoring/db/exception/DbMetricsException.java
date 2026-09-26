package ru.akvine.wild.bot.infrastructure.monitoring.db.exception;

public class DbMetricsException extends RuntimeException {
    public DbMetricsException(String message) {
        super(message);
    }
}
