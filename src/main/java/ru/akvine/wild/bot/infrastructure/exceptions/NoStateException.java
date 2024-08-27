package ru.akvine.wild.bot.infrastructure.exceptions;

public class NoStateException extends RuntimeException {
    public NoStateException(String message) {
        super(message);
    }
}
