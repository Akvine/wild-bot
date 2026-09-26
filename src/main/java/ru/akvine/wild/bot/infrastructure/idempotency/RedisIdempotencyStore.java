package ru.akvine.wild.bot.infrastructure.idempotency;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;

/**
 * Хранилище в Redis ({@link RedisOperationService}): общее для всех инстансов, переживает рестарт.
 * Запись создаётся атомарной командой Redis со сроком жизни, а срок хранения контролирует сам Redis,
 * поэтому очистка не нужна. Ключи вида {@code wild-bot:idempotency:<ключ>}.
 */
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {
    private static final String KEY_PREFIX = "wild-bot:idempotency:";

    private final RedisOperationService<String> redisOperationService;
    private final IdempotencyPayloadSerializer serializer;

    @Override
    public boolean tryBegin(String key, String fingerprint, Duration inProgressTtl) {
        IdempotencyRecord record = new IdempotencyRecord(
                key,
                fingerprint,
                IdempotencyStatus.IN_PROGRESS,
                null,
                LocalDateTime.now().plus(inProgressTtl));
        return redisOperationService.putValueIfAbsentWithTtl(redisKey(key), serializer.toJson(record), inProgressTtl);
    }

    @Override
    public Optional<IdempotencyRecord> find(String key) {
        String json = redisOperationService.getValue(redisKey(key));
        return json == null ? Optional.empty() : Optional.of(serializer.fromJson(json, IdempotencyRecord.class));
    }

    @Override
    public void complete(String key, String payload, Duration resultTtl) {
        Optional<IdempotencyRecord> existing = find(key);
        String fingerprint = existing.map(IdempotencyRecord::fingerprint).orElse(null);
        IdempotencyRecord record = new IdempotencyRecord(
                key,
                fingerprint,
                IdempotencyStatus.COMPLETED,
                payload,
                LocalDateTime.now().plus(resultTtl));
        redisOperationService.putValueWithTtl(redisKey(key), serializer.toJson(record), resultTtl);
    }

    @Override
    public void release(String key) {
        redisOperationService.delete(redisKey(key));
    }

    private static String redisKey(String key) {
        return KEY_PREFIX + key;
    }
}
