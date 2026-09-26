package ru.akvine.wild.bot.infrastructure.http.keystore.exception;

public class AliasDoesNotExistException extends RuntimeException {
    public AliasDoesNotExistException(String message) {
        super(message);
    }

    public AliasDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
