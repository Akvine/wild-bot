package ru.akvine.marketspace.bot.infrastructure.exceptions;

public class NoSessionException extends RuntimeException {
    public NoSessionException(String message) {
        super(message);
    }
}
