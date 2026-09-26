package ru.akvine.wild.bot.infrastructure.idempotency;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Идемпотентное выполнение операций: повторный запрос с тем же ключом не выполняет операцию заново.
 * <ul>
 *     <li>первый вызов с ключом выполняет операцию и сохраняет результат;</li>
 *     <li>повторный вызов после завершения возвращает сохранённый результат;</li>
 *     <li>повторный вызов, пока операция ещё выполняется, - {@link IdempotencyConflictException};</li>
 *     <li>тот же ключ с другим отпечатком запроса - {@link IdempotencyKeyReuseException};</li>
 *     <li>если операция упала, ключ освобождается: клиент может повторить запрос.</li>
 * </ul>
 * Низкоуровневые {@link #begin}/{@link #complete}/{@link #release} нужны там, где результат - не
 * объект, а, например, HTTP-ответ (см. {@link IdempotencyFilter}).
 */
@Slf4j
public class IdempotencyService {
    private static final int BEGIN_ATTEMPTS = 3;

    public enum Outcome {
        /** Ключ свободен и теперь закреплён за вызывающим: операцию нужно выполнить */
        STARTED,
        /** Операция с этим ключом уже завершена: нужно вернуть сохранённый результат */
        REPLAY,
        /** Операция с этим ключом выполняется прямо сейчас */
        IN_PROGRESS,
        /** Ключ уже использован для другого запроса */
        MISMATCH
    }

    /**
     * @param outcome что делать вызывающему
     * @param payload сохранённый результат для {@link Outcome#REPLAY}, иначе {@code null}
     */
    public record BeginResult(Outcome outcome, String payload) {}

    private final IdempotencyStore store;
    private final IdempotencyPayloadSerializer serializer;

    public IdempotencyService(IdempotencyStore store, IdempotencyPayloadSerializer serializer) {
        this.store = Objects.requireNonNull(store, "store");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
    }

    public BeginResult begin(String key, String fingerprint, Duration inProgressTtl) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(fingerprint, "fingerprint");

        for (int attempt = 0; attempt < BEGIN_ATTEMPTS; attempt++) {
            if (store.tryBegin(key, fingerprint, inProgressTtl)) {
                return new BeginResult(Outcome.STARTED, null);
            }
            Optional<IdempotencyRecord> existing = store.find(key);
            if (existing.isEmpty()) {
                continue; // запись истекла или освобождена между двумя обращениями - пробуем занять снова
            }
            IdempotencyRecord record = existing.get();
            if (record.fingerprint() != null && !record.fingerprint().equals(fingerprint)) {
                return new BeginResult(Outcome.MISMATCH, null);
            }
            return record.status() == IdempotencyStatus.COMPLETED
                    ? new BeginResult(Outcome.REPLAY, record.payload())
                    : new BeginResult(Outcome.IN_PROGRESS, null);
        }
        return new BeginResult(Outcome.IN_PROGRESS, null);
    }

    public void complete(String key, String payload, Duration resultTtl) {
        store.complete(key, payload, resultTtl);
    }

    public void release(String key) {
        store.release(key);
    }

    /**
     * Выполняет операцию не более одного раза на ключ.
     *
     * @param resultType тип результата; результат сохраняется как JSON, поэтому должен сериализоваться Jackson'ом
     * @return результат операции: свежий или сохранённый при повторном вызове
     * @throws IdempotencyConflictException операция с этим ключом ещё выполняется
     * @throws IdempotencyKeyReuseException ключ уже использован для другого запроса
     */
    public <T> T executeOnce(
            String key,
            String fingerprint,
            Class<T> resultType,
            Duration inProgressTtl,
            Duration resultTtl,
            Supplier<T> action) {
        BeginResult begin = begin(key, fingerprint, inProgressTtl);
        switch (begin.outcome()) {
            case REPLAY:
                logger.info("Idempotency key [{}] was already processed, returning saved result", key);
                return serializer.fromJson(begin.payload(), resultType);
            case IN_PROGRESS:
                throw new IdempotencyConflictException(key);
            case MISMATCH:
                throw new IdempotencyKeyReuseException(key);
            default:
                break;
        }

        T result;
        try {
            result = action.get();
        } catch (RuntimeException | Error e) {
            release(key);
            throw e;
        }
        complete(key, serializer.toJson(result), resultTtl);
        return result;
    }
}
