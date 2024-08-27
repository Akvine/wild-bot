package ru.akvine.wild.bot.exceptions;

public class CardTypeNotFoundException extends RuntimeException {
    public CardTypeNotFoundException(String message) {
        super(message);
    }
}
