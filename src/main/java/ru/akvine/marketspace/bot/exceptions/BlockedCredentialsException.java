package ru.akvine.marketspace.bot.exceptions;

public class BlockedCredentialsException extends RuntimeException {
    public BlockedCredentialsException(String message) {
        super(message);
    }
}
