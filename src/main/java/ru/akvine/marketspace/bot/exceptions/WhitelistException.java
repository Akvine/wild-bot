package ru.akvine.marketspace.bot.exceptions;

public class WhitelistException extends RuntimeException {
    public WhitelistException(String message) {
        super(message);
    }
}
