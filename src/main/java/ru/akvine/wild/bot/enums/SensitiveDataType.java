package ru.akvine.wild.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SensitiveDataType {
    PASSWORD("password"),
    EMAIL("email"),
    KEY("key"),
    TOKEN("token"),
    SECRET("secret"),
    PHONE("phone"),
    USERNAME("username"),
    ID("id");

    private final String data;
}
