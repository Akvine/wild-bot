package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InformationAmountUtils {
    public int fromMegabytesToBytes(int megabytesCount) {
        validate(megabytesCount);
        return (int) (megabytesCount * Math.pow(10, 6));
    }

    public double fromBytesToMegabytes(int bytes) {
        validate(bytes);
        return bytes / Math.pow(10, 6);
    }

    private void validate(int argument) {
        if (argument < 0) {
            throw new IllegalArgumentException("Argument is negative!");
        }
    }
}
