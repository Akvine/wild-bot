package ru.akvine.wild.bot.exceptions;

public class InvalidDiscountException extends RuntimeException {
    public InvalidDiscountException(String msg) {
        super(msg);
    }
}
