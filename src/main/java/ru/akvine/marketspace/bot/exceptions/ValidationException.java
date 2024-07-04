package ru.akvine.marketspace.bot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationException extends RuntimeException {
    private final String errorCode;
    private final String message;
}
