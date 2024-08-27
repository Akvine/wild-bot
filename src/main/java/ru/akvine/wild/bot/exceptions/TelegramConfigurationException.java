package ru.akvine.wild.bot.exceptions;

public class TelegramConfigurationException extends RuntimeException {
    public TelegramConfigurationException(String message) {
        super(message);
    }
}
