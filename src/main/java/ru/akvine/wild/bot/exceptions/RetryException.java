package ru.akvine.wild.bot.exceptions;

public class RetryException extends RuntimeException {
    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
