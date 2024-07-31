package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreadUtils {
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
