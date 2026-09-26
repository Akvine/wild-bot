package ru.akvine.wild.bot.infrastructure.http.keystore.exception;

public class KeystoreEmptyException extends RuntimeException {
    public KeystoreEmptyException(String message) {
        super(message);
    }

    public KeystoreEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
