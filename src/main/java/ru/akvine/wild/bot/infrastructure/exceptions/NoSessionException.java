package ru.akvine.wild.bot.infrastructure.exceptions;

public class NoSessionException extends RuntimeException {
    public NoSessionException(String message) {
        super(message);
    }
}
