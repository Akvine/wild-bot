package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InformationAmountUtils {
    // TODO : сделать единый метод convert, который будет принимать from, to и count!
    public int fromMegabytesToBytes(int megabytesCount) {
        return (int) (megabytesCount * Math.pow(10, 6));
    }

    public double fromBytesToMegabytes(int bytes) {
        return bytes / Math.pow(10, 6);
    }
}
