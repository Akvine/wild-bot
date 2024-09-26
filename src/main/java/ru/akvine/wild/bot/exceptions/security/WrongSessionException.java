package ru.akvine.wild.bot.exceptions.security;

public class WrongSessionException extends RuntimeException {
    public WrongSessionException(String message) {
        super(message);
    }
}
