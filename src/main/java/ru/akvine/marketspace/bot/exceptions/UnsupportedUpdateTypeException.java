package ru.akvine.marketspace.bot.exceptions;

public class UnsupportedUpdateTypeException extends RuntimeException {
    public UnsupportedUpdateTypeException(String message) {
        super(message);
    }
}
