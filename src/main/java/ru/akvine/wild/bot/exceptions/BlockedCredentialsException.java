package ru.akvine.marketspace.bot.exceptions;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class BlockedCredentialsException extends RuntimeException {
    private final LocalDate blockedDate;

    public BlockedCredentialsException(String message, LocalDate blockedDate) {
        super(message);
        this.blockedDate = blockedDate;
    }
}
