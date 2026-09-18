package ru.akvine.wild.bot.exceptions;

public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException(String msg) {
        super(msg);
    }
}
