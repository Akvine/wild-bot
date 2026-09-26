package ru.akvine.wild.bot.infrastructure.http.keystore.exception;

public class KeystoreBuilderException extends RuntimeException {
    public KeystoreBuilderException(String message) {
        super(message);
    }

    public KeystoreBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
