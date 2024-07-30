package ru.akvine.marketspace.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum IterationsCounterServiceType {
    IN_MEMORY("in_memory"),
    DATABASE("database");

    private final String type;

    public static IterationsCounterServiceType safeValueOf(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Iterations counter service type can't be null or empty!");
        }

        return switch (value.toLowerCase()) {
            case "in_memory" -> IN_MEMORY;
            case "database" -> DATABASE;
            default ->
                    throw new IllegalArgumentException("Iterations counter service with type = [" + value + "] not supported!");
        };
    }
}
