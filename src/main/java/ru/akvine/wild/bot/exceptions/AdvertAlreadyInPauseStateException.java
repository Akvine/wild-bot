package ru.akvine.wild.bot.exceptions;

public class AdvertAlreadyInPauseStateException extends RuntimeException {
    public AdvertAlreadyInPauseStateException(String message) {
        super(message);
    }
}
