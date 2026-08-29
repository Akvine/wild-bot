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
    BENCHMARK("benchmark"),
    TRACING("tracing"),
    TRANSACTIONAL("transactional"),
    RETRY("retry"),
    EXCEPTION("exception"),
    RATELIMITING("ratelimiting"),
    VALIDATION("validation");

    private final String value;

    public static ProxyType safeValueOf(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Proxy type is blank!");
        }

        for (ProxyType type : values()) {
            if (value.equalsIgnoreCase(type.getValue())) {
                return type;
            }
        }

        throw new IllegalArgumentException("Proxy type [" + value + "] is not supported by app!");
    }
}
