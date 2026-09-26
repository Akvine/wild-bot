package ru.akvine.wild.bot.unit.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.infrastructure.idempotency.Fingerprints;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyConflictException;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyKeyReuseException;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyPayloadSerializer;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService.Outcome;
import ru.akvine.wild.bot.infrastructure.idempotency.InMemoryIdempotencyStore;

class IdempotencyServiceTest {
    private static final Duration IN_PROGRESS_TTL = Duration.ofMinutes(5);
    private static final Duration RESULT_TTL = Duration.ofHours(1);

    /** Результат операции: то, что сохраняется и возвращается при повторе */
    public static class Result {
        public String value;
        public int number;

        public Result() {}

        Result(String value, int number) {
            this.value = value;
            this.number = number;
        }
    }

    private final IdempotencyService service =
            new IdempotencyService(new InMemoryIdempotencyStore(), new IdempotencyPayloadSerializer());

    private Result execute(String key, String fingerprint, AtomicInteger executions) {
        return service.executeOnce(key, fingerprint, Result.class, IN_PROGRESS_TTL, RESULT_TTL, () -> {
            executions.incrementAndGet();
            return new Result("created", 42);
        });
    }

    @Test
    @DisplayName("Повторный вызов с тем же ключом не выполняет операцию и возвращает сохранённый результат")
    void repeatedCallReturnsSavedResultWithoutExecutingAgain() {
        AtomicInteger executions = new AtomicInteger();

        Result first = execute("key-1", "fp", executions);
        Result second = execute("key-1", "fp", executions);

        assertThat(executions).hasValue(1);
        assertThat(first.value).isEqualTo("created");
        assertThat(second.value).isEqualTo("created");
        assertThat(second.number).isEqualTo(42);
    }

    @Test
    @DisplayName("Разные ключи - независимые операции")
    void differentKeysAreIndependent() {
        AtomicInteger executions = new AtomicInteger();

        execute("key-1", "fp", executions);
        execute("key-2", "fp", executions);

        assertThat(executions).hasValue(2);
    }

    @Test
    @DisplayName("Тот же ключ с другим отпечатком запроса - ошибка клиента, а не повтор")
    void sameKeyWithDifferentFingerprintIsRejected() {
        AtomicInteger executions = new AtomicInteger();
        execute("key-1", "fp-1", executions);

        assertThatThrownBy(() -> execute("key-1", "fp-2", executions)).isInstanceOf(IdempotencyKeyReuseException.class);

        assertThat(executions).hasValue(1);
    }

    @Test
    @DisplayName("Если операция упала, ключ освобождается и запрос можно повторить")
    void failedOperationReleasesKey() {
        AtomicInteger executions = new AtomicInteger();

        assertThatThrownBy(() -> service.executeOnce(
                        "key-1", "fp", Result.class, IN_PROGRESS_TTL, RESULT_TTL, () -> {
                            executions.incrementAndGet();
                            throw new IllegalStateException("boom");
                        }))
                .isInstanceOf(IllegalStateException.class);
        Result retried = execute("key-1", "fp", executions);

        assertThat(executions).hasValue(2);
        assertThat(retried.value).isEqualTo("created");
    }

    @Test
    @DisplayName("Пока операция выполняется, повторный вызов получает конфликт")
    void callDuringExecutionGetsConflict() {
        AtomicInteger executions = new AtomicInteger();

        service.executeOnce("key-1", "fp", Result.class, IN_PROGRESS_TTL, RESULT_TTL, () -> {
            executions.incrementAndGet();
            assertThatThrownBy(() -> execute("key-1", "fp", new AtomicInteger()))
                    .isInstanceOf(IdempotencyConflictException.class);
            return new Result("created", 1);
        });

        assertThat(executions).hasValue(1);
    }

    @Test
    @DisplayName("Истёкший ключ можно использовать заново")
    void expiredKeyCanBeReused() {
        AtomicInteger executions = new AtomicInteger();
        // отрицательный срок: запись сразу считается истёкшей
        service.executeOnce("key-1", "fp", Result.class, IN_PROGRESS_TTL, Duration.ofMillis(-1), () -> {
            executions.incrementAndGet();
            return new Result("first", 1);
        });

        Result second = execute("key-1", "fp", executions);

        assertThat(executions).hasValue(2);
        assertThat(second.value).isEqualTo("created");
    }

    @Test
    @DisplayName("Начало операции: STARTED, затем IN_PROGRESS, после завершения REPLAY с результатом")
    void beginOutcomesFollowLifecycle() {
        assertThat(service.begin("key-1", "fp", IN_PROGRESS_TTL).outcome()).isEqualTo(Outcome.STARTED);
        assertThat(service.begin("key-1", "fp", IN_PROGRESS_TTL).outcome()).isEqualTo(Outcome.IN_PROGRESS);
        assertThat(service.begin("key-1", "other", IN_PROGRESS_TTL).outcome()).isEqualTo(Outcome.MISMATCH);

        service.complete("key-1", "saved", RESULT_TTL);

        IdempotencyService.BeginResult replay = service.begin("key-1", "fp", IN_PROGRESS_TTL);
        assertThat(replay.outcome()).isEqualTo(Outcome.REPLAY);
        assertThat(replay.payload()).isEqualTo("saved");
        assertThat(service.begin("key-1", "other", IN_PROGRESS_TTL).outcome()).isEqualTo(Outcome.MISMATCH);
    }

    @Test
    @DisplayName("При одновременных запросах с одним ключом операция выполняется ровно один раз")
    void concurrentCallsExecuteOperationExactlyOnce() throws Exception {
        int threads = 16;
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Outcome>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return service.begin("key-1", "fp", IN_PROGRESS_TTL).outcome();
                }));
            }
            start.countDown();

            for (Future<Outcome> future : futures) {
                if (future.get() == Outcome.STARTED) {
                    executions.incrementAndGet();
                }
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(executions).hasValue(1);
    }

    @Test
    @DisplayName("Отпечаток зависит от каждой части запроса и от границ между частями")
    void fingerprintDependsOnEveryPart() {
        assertThat(Fingerprints.of("POST", "/a", "x")).isEqualTo(Fingerprints.of("POST", "/a", "x"));
        assertThat(Fingerprints.of("POST", "/a", "x")).isNotEqualTo(Fingerprints.of("POST", "/a", "y"));
        assertThat(Fingerprints.of("ab", "c")).isNotEqualTo(Fingerprints.of("a", "bc"));
        assertThat(Fingerprints.of("body".getBytes(), "POST")).isNotEqualTo(Fingerprints.of("Body".getBytes(), "POST"));
    }
}
