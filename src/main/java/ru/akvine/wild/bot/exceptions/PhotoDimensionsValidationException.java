package ru.akvine.wild.bot.exceptions;

import lombok.Getter;

@Getter
public class PhotoDimensionsValidationException extends RuntimeException {
    private final int width;
    private final int height;

    public PhotoDimensionsValidationException(String message, int width, int height) {
        super(message);
        this.width = width;
        this.height = height;
    }
}
