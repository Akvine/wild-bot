package ru.akvine.wild.bot.utils;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UUIDGenerator {
    private static final int START_INDEX = 0;

    public String uuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public String uuid(int length) {
        return uuid().substring(START_INDEX, length);
    }

    public String uuidWithoutDashes() {
        return uuid().replaceAll("-", "");
    }
}
