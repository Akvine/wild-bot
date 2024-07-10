package ru.akvine.marketspace.bot.exceptions;

public class ClientWhitelistException extends RuntimeException {
    public ClientWhitelistException(String message) {
        super(message);
    }
}
