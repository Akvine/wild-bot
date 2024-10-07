package ru.akvine.wild.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum ProxyType {
    LOCK("lock"),
    SECURITY("security"),
    STATISTIC("statistic"),
    LOGGING("logging"),
    CACHE("cache"),
    IDEMPOTENCE("idempotence"),
    METRICS("metrics"),
    PRECONDITIONS("preconditions"),
    BENCHMARK("benchmark");

    private final String value;

    public static ProxyType safeValueOf(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Proxy type is blank!");
        }

        return switch (value.toLowerCase()) {
            case "lock" -> LOCK;
            case "security" -> SECURITY;
            case "statistic" -> STATISTIC;
            case "logging" -> LOGGING;
            case "cache" -> CACHE;
            case "idempotence" -> IDEMPOTENCE;
            case "metrics" -> METRICS;
            case "preconditions" -> PRECONDITIONS;
            case "benchmark" -> BENCHMARK;
            default -> throw new IllegalArgumentException("Proxy type with value = [" + value + "] is not supported!");
        };
    }
}
