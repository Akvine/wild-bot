package ru.akvine.marketspace.bot.exceptions;

import lombok.Getter;

@Getter
public class StartAdvertException extends RuntimeException {
    private final String exceptionMessage;
    public StartAdvertException(String message, String exceptionMessage) {
        super(message);
        this.exceptionMessage = exceptionMessage;
    }
}
