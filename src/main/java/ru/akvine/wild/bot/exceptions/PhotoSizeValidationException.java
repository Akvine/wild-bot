package ru.akvine.wild.bot.exceptions;

import lombok.Getter;

@Getter
public class PhotoSizeValidationException extends RuntimeException {
    private final double megabytes;

    public PhotoSizeValidationException(String message, double megabytes) {
        super(message);
        this.megabytes = megabytes;
    }
}
