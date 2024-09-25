package ru.akvine.wild.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationProviderType {
    LOG("log", true),
    CONSTANT("constant", true);

    private final String code;
    private final boolean dummy;

    public static NotificationProviderType safeValueOf(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Notification provider type can't be null");
        }

        switch (type.toLowerCase()) {
            case "log": return LOG;
            case "constant": return CONSTANT;
            default:
                throw new IllegalArgumentException("Notification provider type = [" + type + "] is not supported!");
        }
    }
}
