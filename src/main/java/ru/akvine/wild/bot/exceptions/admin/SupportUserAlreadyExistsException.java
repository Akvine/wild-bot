package ru.akvine.wild.bot.exceptions.admin;

public class SupportUserAlreadyExistsException extends RuntimeException {
    public SupportUserAlreadyExistsException(String message) {
        super(message);
    }
}
