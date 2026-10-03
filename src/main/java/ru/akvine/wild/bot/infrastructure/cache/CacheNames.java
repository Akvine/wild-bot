package ru.akvine.wild.bot.infrastructure.cache;

/**
 * Имена кешей Spring Cache. Кеш включён в {@code RedisConfig} ({@code @EnableCaching}): при {@code spring.redis.enabled=true}
 * значения лежат в Redis с TTL, иначе - в памяти процесса (стандартный {@code ConcurrentMapCacheManager} Spring Boot).
 */
public final class CacheNames {
    /** Справочник типов карточек ("Мужской", "Женский", ...) по названию типа. Меняется только синхронизацией с WB */
    public static final String CARD_TYPES = "card-types";

    private CacheNames() {}
}
