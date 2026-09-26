package ru.akvine.wild.bot.infrastructure.counter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.exceptions.IterationCounterNotFoundException;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;

/**
 * Реализация {@link CountersStorage} поверх Redis ({@link RedisOperationService}): счётчик -
 * атомарное число в Redis, переживает рестарт приложения и общий для всех инстансов, поэтому
 * {@link #increase} корректен и при одновременной работе нескольких инстансов. Ключи вида
 * {@code wild-bot:counters:<advertId>}; {@link #delete} должен вызываться, когда кампания
 * останавливается.
 */
@RequiredArgsConstructor
@Slf4j
public class CountersStorageInRedisImpl implements CountersStorage {
    private static final String KEY_PREFIX = "wild-bot:counters:";

    private final RedisOperationService<Long> redisOperationService;

    @Override
    public void add(int advertId) {
        logger.debug("Init redis counter for advert with id = {}", advertId);
        redisOperationService.saveChunkValue(key(advertId), (long) ZERO_COUNT_INIT);
    }

    @Override
    public void increase(int advertId) {
        validateExists(advertId);
        Long total = redisOperationService.incrementAndGetChunkValue(key(advertId));
        logger.info("Increase redis counter for advert with id = {}, total = {}", advertId, total);
    }

    @Override
    public void delete(int advertId) {
        validateExists(advertId);
        redisOperationService.delete(key(advertId));
        logger.info("Delete redis counter for advert with id = {}", advertId);
    }

    @Override
    public boolean check(int advertId, int maxCountBeforeIncrease) {
        validateExists(advertId);
        return redisOperationService.getChunkValue(key(advertId)) % maxCountBeforeIncrease == 0;
    }

    private void validateExists(int advertId) {
        if (!redisOperationService.hasKey(key(advertId))) {
            String errorMessage = String.format("Iteration counter with advert id = [%s] not found!", advertId);
            throw new IterationCounterNotFoundException(errorMessage);
        }
    }

    private static String key(int advertId) {
        return KEY_PREFIX + advertId;
    }
}
