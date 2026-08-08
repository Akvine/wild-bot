package ru.akvine.wild.bot.exceptions;

public class BotHandlingException extends RuntimeException {
    public BotHandlingException(Exception exception) {
        super(exception);
    }
}
