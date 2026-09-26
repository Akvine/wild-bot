package ru.akvine.wild.bot.infrastructure.idempotency;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище в памяти процесса: не переживает рестарт и не виден другим инстансам, поэтому защищает
 * от дублей только в пределах одного инстанса. Истёкшие записи удаляются при обращении к ключу и
 * при каждой записи нового ключа.
 */
public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Map<String, IdempotencyRecord> records = new ConcurrentHashMap<>();

    @Override
    public boolean tryBegin(String key, String fingerprint, Duration inProgressTtl) {
        LocalDateTime now = LocalDateTime.now();
        removeExpired(now);
        boolean[] created = {false};
        records.compute(key, (k, existing) -> {
            if (existing != null && !existing.isExpired(now)) {
                return existing;
            }
            created[0] = true;
            return new IdempotencyRecord(
                    key, fingerprint, IdempotencyStatus.IN_PROGRESS, null, now.plus(inProgressTtl));
        });
        return created[0];
    }

    @Override
    public Optional<IdempotencyRecord> find(String key) {
        IdempotencyRecord record = records.get(key);
        if (record == null || record.isExpired(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(record);
    }

    @Override
    public void complete(String key, String payload, Duration resultTtl) {
        records.computeIfPresent(
                key,
                (k, existing) -> new IdempotencyRecord(
                        key,
                        existing.fingerprint(),
                        IdempotencyStatus.COMPLETED,
                        payload,
                        LocalDateTime.now().plus(resultTtl)));
    }

    @Override
    public void release(String key) {
        records.remove(key);
    }

    private void removeExpired(LocalDateTime now) {
        records.values().removeIf(record -> record.isExpired(now));
    }
}
