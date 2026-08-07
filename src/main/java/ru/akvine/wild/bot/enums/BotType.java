package ru.akvine.wild.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotType {
    TELEGRAM("telegram"),
    MAX("max");

    private final String type;
}
