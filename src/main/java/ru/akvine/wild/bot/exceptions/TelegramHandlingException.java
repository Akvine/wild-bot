package ru.akvine.marketspace.bot.exceptions;

public class TelegramHandlingException extends RuntimeException {
    public TelegramHandlingException(Exception exception) {
        super(exception);
    }
}