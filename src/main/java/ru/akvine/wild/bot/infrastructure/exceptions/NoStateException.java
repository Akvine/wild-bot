package ru.akvine.marketspace.bot.infrastructure.exceptions;

public class NoStateException extends RuntimeException {
    public NoStateException(String message) {
        super(message);
    }
}
