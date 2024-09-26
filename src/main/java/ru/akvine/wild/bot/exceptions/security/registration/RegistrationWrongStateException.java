package ru.akvine.wild.bot.exceptions.security.registration;

public class RegistrationWrongStateException extends RuntimeException {
    public RegistrationWrongStateException(String message) {
        super(message);
    }
}
