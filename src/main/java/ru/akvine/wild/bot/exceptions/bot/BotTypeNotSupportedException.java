package ru.akvine.wild.bot.exceptions.bot;

public class BotTypeNotSupportedException extends RuntimeException {
    public BotTypeNotSupportedException(String message) {
        super(message);
    }
}
