package ru.akvine.wild.bot.exceptions.bot;

public class InvalidUserMessageException extends RuntimeException {
    public InvalidUserMessageException(String message) {
        super(message);
    }
}
