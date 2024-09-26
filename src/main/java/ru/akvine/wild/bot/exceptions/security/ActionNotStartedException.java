package ru.akvine.wild.bot.exceptions.security;

public class ActionNotStartedException extends RuntimeException {
    public ActionNotStartedException(String message) {
        super(message);
    }
}
