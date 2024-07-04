package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UUIDGenerator {
    private final static int START_INDEX = 0;

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String uuid(int length) {
        return uuid().substring(START_INDEX, length);
    }

    public String uuidWithoutDashes(int length) {
        return uuid(length).replaceAll("-", "");
    }
}
