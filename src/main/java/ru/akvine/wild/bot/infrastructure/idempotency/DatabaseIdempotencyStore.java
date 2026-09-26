package ru.akvine.wild.bot.infrastructure.idempotency;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.infrastructure.IdempotencyKeyEntity;
import ru.akvine.wild.bot.repositories.infrastructure.IdempotencyKeyRepository;

/**
 * Хранилище в БД ({@code IDEMPOTENCY_KEY_ENTITY}): общее для всех инстансов, переживает рестарт.
 * Атомарность даёт первичный ключ таблицы и вставка {@code on conflict do nothing}. Каждая операция
 * выполняется в отдельной транзакции, поэтому запись не откатывается вместе с транзакцией
 * вызывающего кода. Истёкшие записи чистит {@link #deleteExpired()}.
 */
@RequiredArgsConstructor
public class DatabaseIdempotencyStore implements IdempotencyStore {
    private final IdempotencyKeyRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryBegin(String key, String fingerprint, Duration inProgressTtl) {
        LocalDateTime now = LocalDateTime.now();
        repository.deleteExpiredByKey(key, now);
        return repository.insertIfAbsent(key, fingerprint, now.plus(inProgressTtl), now) == 1;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IdempotencyRecord> find(String key) {
        LocalDateTime now = LocalDateTime.now();
        return repository
                .findById(key)
                .map(entity -> new IdempotencyRecord(
                        entity.getIdempotencyKey(),
                        entity.getFingerprint(),
                        entity.getStatus(),
                        entity.getPayload(),
                        entity.getExpiresAt()))
                .filter(record -> !record.isExpired(now));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String key, String payload, Duration resultTtl) {
        IdempotencyKeyEntity entity = repository.findById(key).orElse(null);
        if (entity == null) {
            return;
        }
        entity.setStatus(IdempotencyStatus.COMPLETED);
        entity.setPayload(payload);
        entity.setExpiresAt(LocalDateTime.now().plus(resultTtl));
        entity.setUpdatedDate(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(String key) {
        repository.deleteByKey(key);
    }

    /**
     * Удаляет истёкшие записи
     *
     * @return сколько записей удалено
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteExpired() {
        return repository.deleteExpired(LocalDateTime.now());
    }
}
