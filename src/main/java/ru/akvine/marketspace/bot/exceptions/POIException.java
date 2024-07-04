package ru.akvine.marketspace.bot.exceptions;

public class POIException extends RuntimeException {
    public POIException(Exception exception) {
        super(exception);
    }
}
