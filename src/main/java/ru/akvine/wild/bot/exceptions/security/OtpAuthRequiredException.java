package ru.akvine.wild.bot.exceptions.security;

public class OtpAuthRequiredException extends RuntimeException {
    public OtpAuthRequiredException(String message) {
        super(message);
    }
}
