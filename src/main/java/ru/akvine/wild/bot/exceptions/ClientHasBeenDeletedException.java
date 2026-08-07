package ru.akvine.wild.bot.exceptions;

public class ClientHasBeenDeletedException extends RuntimeException {
    public ClientHasBeenDeletedException(String message) {
        super(message);
    }
}
