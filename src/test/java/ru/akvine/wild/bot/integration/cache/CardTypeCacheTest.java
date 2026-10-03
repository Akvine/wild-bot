package ru.akvine.wild.bot.integration.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import ru.akvine.wild.bot.config.RedisConfig;
import ru.akvine.wild.bot.exceptions.CardTypeNotFoundException;
import ru.akvine.wild.bot.infrastructure.cache.CacheNames;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.services.CardTypeService;
import ru.akvine.wild.bot.services.domain.CardTypeInfo;

/**
 * Cache-Aside для справочника типов карточек: первый вызов читает БД, повторные берут значение из кеша, отсутствующий
 * тип не кешируется, сброс заставляет снова читать БД.
 */
@DisplayName("Spring Cache: справочник типов карточек")
class CardTypeCacheTest extends BaseTest {
    @Autowired
    private CardTypeService cardTypeService;

    @Autowired
    private CacheManager cacheManager;

    @SpyBean
    private CardTypeRepository cardTypeRepository;

    @BeforeEach
    void setUp() {
        cardTypeService.evictCache();
        clearInvocations(cardTypeRepository);
    }

    @Test
    @DisplayName("Повторный запрос типа берётся из кеша: репозиторий вызывается один раз")
    void secondCallIsServedFromCache() {
        CardTypeInfo first = cardTypeService.getByType("Мужской");
        CardTypeInfo second = cardTypeService.getByType("Мужской");

        assertThat(first.type()).isEqualTo("Мужской");
        assertThat(second).isEqualTo(first);
        verify(cardTypeRepository, times(1)).findByType("Мужской");
        Cache cache = cacheManager.getCache(CacheNames.CARD_TYPES);
        assertThat(cache).isNotNull();
        assertThat(cache.get("Мужской")).isNotNull();
    }

    @Test
    @DisplayName("Разные типы кешируются раздельно")
    void differentTypesAreCachedSeparately() {
        cardTypeService.getByType("Мужской");
        cardTypeService.getByType("Женский");
        cardTypeService.getByType("Мужской");
        cardTypeService.getByType("Женский");

        verify(cardTypeRepository, times(1)).findByType("Мужской");
        verify(cardTypeRepository, times(1)).findByType("Женский");
    }

    @Test
    @DisplayName("Отсутствующий тип не кешируется: каждый вызов идёт в БД и бросает исключение")
    void missingTypeIsNotCached() {
        assertThatThrownBy(() -> cardTypeService.getByType("Несуществующий"))
                .isInstanceOf(CardTypeNotFoundException.class);
        assertThatThrownBy(() -> cardTypeService.getByType("Несуществующий"))
                .isInstanceOf(CardTypeNotFoundException.class);

        verify(cardTypeRepository, times(2)).findByType("Несуществующий");
        assertThat(cacheManager.getCache(CacheNames.CARD_TYPES).get("Несуществующий"))
                .isNull();
    }

    @Test
    @DisplayName("После сброса кеша значение снова читается из БД")
    void evictForcesReload() {
        cardTypeService.getByType("Мужской");

        cardTypeService.evictCache();
        cardTypeService.getByType("Мужской");

        verify(cardTypeRepository, times(2)).findByType("Мужской");
    }

    @Test
    @DisplayName("Redis-кеш: для справочника типов TTL час, для остальных кешей - минута")
    void redisCacheManagerUsesLongerTtlForCardTypes() {
        RedisConfig config = new RedisConfig();

        RedisCacheManager manager = config.cacheManager(mock(RedisConnectionFactory.class));
        manager.initializeCaches();

        // менеджер transaction-aware: настоящий RedisCache спрятан внутри декоратора
        Cache cardTypes = manager.getCache(CacheNames.CARD_TYPES);
        Cache other = manager.getCache("any-other-cache");
        RedisCache cardTypesCache = (RedisCache) ((TransactionAwareCacheDecorator) cardTypes).getTargetCache();
        RedisCache otherCache = (RedisCache) ((TransactionAwareCacheDecorator) other).getTargetCache();

        assertThat(cardTypesCache.getCacheConfiguration().getTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(otherCache.getCacheConfiguration().getTtl()).isEqualTo(Duration.ofMinutes(1));
    }
}
