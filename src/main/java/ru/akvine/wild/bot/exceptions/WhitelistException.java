package ru.akvine.wild.bot.exceptions;

public class WhitelistException extends RuntimeException {
    public WhitelistException(String message) {
        super(message);
    }
}
