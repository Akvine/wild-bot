package ru.akvine.wild.bot.exceptions.security;

public class NoMoreOtpInvalidAttemptsException extends RuntimeException {
    public NoMoreOtpInvalidAttemptsException(String message) {
        super(message);
    }
}
