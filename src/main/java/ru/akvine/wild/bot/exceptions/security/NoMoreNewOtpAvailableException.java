package ru.akvine.wild.bot.exceptions.security;

public class NoMoreNewOtpAvailableException extends RuntimeException {
    private final boolean userWasBlocked;

    public NoMoreNewOtpAvailableException(String message, boolean userWasBlocked) {
        super(message);
        this.userWasBlocked = userWasBlocked;
    }

    public NoMoreNewOtpAvailableException(String message) {
        this(message, true);
    }

    public boolean userWasBlocked() {return userWasBlocked;}
}
