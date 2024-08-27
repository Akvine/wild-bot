package ru.akvine.wild.bot.exceptions;

public class HasNoSubscriptionException extends RuntimeException {
    public HasNoSubscriptionException(String message) {
        super(message);
    }
}
