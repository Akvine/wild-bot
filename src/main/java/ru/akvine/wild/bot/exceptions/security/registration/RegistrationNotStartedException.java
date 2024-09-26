package ru.akvine.wild.bot.exceptions.security.registration;

public class RegistrationNotStartedException extends RuntimeException {
    public RegistrationNotStartedException(String message) {
        super(message);
    }
}
