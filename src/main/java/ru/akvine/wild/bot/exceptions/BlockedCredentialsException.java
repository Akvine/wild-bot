package ru.akvine.wild.bot.exceptions;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class BlockedCredentialsException extends RuntimeException {
    private final LocalDate blockedDate;

    public BlockedCredentialsException(String message, LocalDate blockedDate) {
        super(message);
        this.blockedDate = blockedDate;
    }
}
