package ru.akvine.wild.bot.infrastructure.http.keystore.exception;

public class KeystoreConversionException extends RuntimeException {
    public KeystoreConversionException(String message) {
        super(message);
    }

    public KeystoreConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
