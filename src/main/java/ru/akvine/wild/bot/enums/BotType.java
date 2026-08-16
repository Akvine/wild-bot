package ru.akvine.wild.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BotType {
    TELEGRAM("telegram"),
    MAX("max");

    private final String type;

    public static BotType safeValueOf(String value) {
        for (BotType botType : values()) {
            if (botType.getType().equalsIgnoreCase(value)) {
                return botType;
            }
        }

        throw new IllegalArgumentException("Bot type = [" + value + "] is not supported by app!");
    }
}
