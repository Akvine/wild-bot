package ru.akvine.wild.bot.exceptions;

public class RetryException extends RuntimeException {
    public RetryException(String message) {
        super(message);
    }
}
