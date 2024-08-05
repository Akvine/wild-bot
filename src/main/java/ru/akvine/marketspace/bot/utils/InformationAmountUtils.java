package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InformationAmountUtils {
    public int fromMegabytesToBytes(int megabytesCount) {
        return (int) (megabytesCount * Math.pow(10, 6));
    }

    public double fromBytesToMegabytes(int bytes) {
        return bytes / Math.pow(10, 6);
    }
}
