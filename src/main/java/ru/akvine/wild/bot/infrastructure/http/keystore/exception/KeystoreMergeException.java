package ru.akvine.wild.bot.infrastructure.http.keystore.exception;

public class KeystoreMergeException extends RuntimeException {
    public KeystoreMergeException(String message) {
        super(message);
    }

    public KeystoreMergeException(String message, Throwable cause) {
        super(message, cause);
    }
}
