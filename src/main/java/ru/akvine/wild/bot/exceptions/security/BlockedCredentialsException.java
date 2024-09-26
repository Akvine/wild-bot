package ru.akvine.wild.bot.exceptions.security;

public class BlockedCredentialsException extends RuntimeException {
    public BlockedCredentialsException(String message) {
        super(message);
    }
}
