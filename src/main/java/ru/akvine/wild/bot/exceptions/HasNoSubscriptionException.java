package ru.akvine.marketspace.bot.exceptions;

public class HasNoSubscriptionException extends RuntimeException {
    public HasNoSubscriptionException(String message) {
        super(message);
    }
}
