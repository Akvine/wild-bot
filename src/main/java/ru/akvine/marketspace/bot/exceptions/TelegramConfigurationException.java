package ru.akvine.marketspace.bot.exceptions;

public class TelegramConfigurationException extends RuntimeException {
    public TelegramConfigurationException(String message) {
        super(message);
    }
}
