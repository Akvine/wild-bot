package ru.akvine.wild.bot.exceptions;

public class IntegrationException extends RuntimeException {
    public IntegrationException(String message) {
        super(message);
    }

    public IntegrationException(Exception exception) {
        super(exception);
    }
}
