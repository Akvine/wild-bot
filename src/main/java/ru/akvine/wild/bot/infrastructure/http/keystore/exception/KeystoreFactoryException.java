package ru.akvine.wild.bot.infrastructure.http.keystore.exception;

public class KeystoreFactoryException extends RuntimeException {
    public KeystoreFactoryException(String message) {
        super(message);
    }

    public KeystoreFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
