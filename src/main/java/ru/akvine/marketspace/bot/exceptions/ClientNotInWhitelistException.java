package ru.akvine.marketspace.bot.exceptions;

public class ClientNotInWhitelistException extends RuntimeException {
    public ClientNotInWhitelistException(String message) {
        super(message);
    }
}
