package ru.akvine.wild.bot.exceptions.admin;

public class SupportUserNotFoundException extends RuntimeException {
    public SupportUserNotFoundException(String message) {
        super(message);
    }
}
