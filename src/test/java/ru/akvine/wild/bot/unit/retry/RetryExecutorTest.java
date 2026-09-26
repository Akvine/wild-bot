package ru.akvine.wild.bot.unit.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.exceptions.RetryException;
import ru.akvine.wild.bot.infrastructure.retry.AbstractRetryExecutor.Sleeper;
import ru.akvine.wild.bot.infrastructure.retry.DefaultRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.ExponentialRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.RetryExecutor;

class RetryExecutorTest {

    private static final String MESSAGE = "Doing work";

    private final List<Duration> pauses = new ArrayList<>();
    private final Sleeper recordingSleeper = pauses::add;

    private RetryExecutor fixed(int attempts) {
        return new DefaultRetryExecutor(attempts, Duration.ofMillis(100), recordingSleeper);
    }

    private RetryExecutor exponential(int attempts) {
        return new ExponentialRetryExecutor(
                attempts, Duration.ofMillis(100), 2.0, Duration.ofMillis(350), 0.0, recordingSleeper);
    }

    @Test
    @DisplayName("Runnable, выполненный успешно, запускается ровно один раз и без пауз")
    void runnableSuccessRunsOnce() {
        AtomicInteger runs = new AtomicInteger();

        fixed(3).execute((Runnable) runs::incrementAndGet, MESSAGE);

        assertThat(runs).hasValue(1);
        assertThat(pauses).isEmpty();
    }

    @Test
    @DisplayName("Supplier возвращает результат первой удачной попытки")
    void supplierReturnsResultOfSuccessfulAttempt() {
        AtomicInteger calls = new AtomicInteger();

        String result = exponential(3)
                .execute(
                        () -> {
                            if (calls.incrementAndGet() < 3) {
                                throw new IllegalStateException("fail #" + calls.get());
                            }
                            return "ok";
                        },
                        MESSAGE);

        assertThat(result).isEqualTo("ok");
        assertThat(calls).hasValue(3);
        assertThat(pauses).hasSize(2);
    }

    @Test
    @DisplayName("Когда попытки исчерпаны, бросается RetryException с последней ошибкой в причине")
    void exhaustedAttemptsThrowRetryExceptionWithLastCause() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> fixed(3).execute(
                                (Runnable) () -> {
                                    throw new IllegalStateException("fail #" + calls.incrementAndGet());
                                },
                                MESSAGE))
                .isInstanceOf(RetryException.class)
                .hasMessageContaining(MESSAGE)
                .hasMessageContaining("[3]")
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("fail #3");

        assertThat(calls).hasValue(3);
        assertThat(pauses).hasSize(2);
    }

    @Test
    @DisplayName("Фиксированная задержка одинакова между всеми попытками")
    void fixedDelayIsConstant() {
        assertThatThrownBy(() -> fixed(4).execute(
                                (Runnable) () -> {
                                    throw new IllegalStateException();
                                },
                                MESSAGE))
                .isInstanceOf(RetryException.class);

        assertThat(pauses).containsExactly(Duration.ofMillis(100), Duration.ofMillis(100), Duration.ofMillis(100));
    }

    @Test
    @DisplayName("Экспоненциальная задержка растёт по множителю и ограничена maxDelay")
    void exponentialDelayGrowsAndIsCapped() {
        assertThatThrownBy(() -> exponential(5)
                        .execute(
                                (Runnable) () -> {
                                    throw new IllegalStateException();
                                },
                                MESSAGE))
                .isInstanceOf(RetryException.class);

        assertThat(pauses)
                .containsExactly(
                        Duration.ofMillis(100), Duration.ofMillis(200), Duration.ofMillis(350), Duration.ofMillis(350));
    }

    @Test
    @DisplayName("Джиттер не выводит задержку за пределы [delay*(1-j), maxDelay]")
    void jitterStaysWithinBounds() {
        RetryExecutor executor = new ExponentialRetryExecutor(
                30, Duration.ofMillis(100), 2.0, Duration.ofMillis(500), 0.5, recordingSleeper);

        assertThatThrownBy(() -> executor.execute(
                        (Runnable) () -> {
                            throw new IllegalStateException();
                        },
                        MESSAGE))
                .isInstanceOf(RetryException.class);

        assertThat(pauses).hasSize(29).allSatisfy(pause -> assertThat(pause.toMillis())
                .isBetween(50L, 500L));
    }

    @Test
    @DisplayName("Исключение, не прошедшее предикат, не повторяется и пробрасывается как есть")
    void nonRetryableRuntimeExceptionIsRethrownAsIs() {
        AtomicInteger calls = new AtomicInteger();
        IllegalArgumentException original = new IllegalArgumentException("bad input");

        assertThatThrownBy(() -> fixed(3).execute(
                                (Runnable) () -> {
                                    calls.incrementAndGet();
                                    throw original;
                                },
                                e -> !(e instanceof IllegalArgumentException),
                                MESSAGE))
                .isSameAs(original);

        assertThat(calls).hasValue(1);
        assertThat(pauses).isEmpty();
    }

    @Test
    @DisplayName("Проверяемое исключение из Callable оборачивается в RetryException")
    void checkedExceptionIsWrapped() {
        assertThatThrownBy(() -> fixed(2).call(
                                () -> {
                                    throw new IOException("disk");
                                },
                                RetryExecutor.RETRY_ON_ANY,
                                MESSAGE))
                .isInstanceOf(RetryException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Прерывание во время паузы останавливает повторы и сохраняет флаг прерывания")
    void interruptionDuringSleepStopsRetries() {
        AtomicInteger calls = new AtomicInteger();
        RetryExecutor executor = new DefaultRetryExecutor(5, Duration.ofMillis(100), duration -> {
            throw new InterruptedException("stop");
        });

        try {
            assertThatThrownBy(() -> executor.execute(
                            (Runnable) () -> {
                                calls.incrementAndGet();
                                throw new IllegalStateException("fail");
                            },
                            MESSAGE))
                    .isInstanceOf(RetryException.class)
                    .hasCauseInstanceOf(InterruptedException.class)
                    .satisfies(e -> assertThat(e.getSuppressed()).hasSize(1));

            assertThat(calls).hasValue(1);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted(); // не оставляем флаг прерывания другим тестам
        }
    }

    @Test
    @DisplayName("Некорректные параметры отклоняются в конструкторе")
    void invalidParametersAreRejected() {
        assertThatThrownBy(() -> new DefaultRetryExecutor(0, Duration.ofMillis(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DefaultRetryExecutor(1, Duration.ofMillis(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExponentialRetryExecutor(3, Duration.ofMillis(100), 0.5, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExponentialRetryExecutor(3, Duration.ofSeconds(5), 2, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExponentialRetryExecutor(3, Duration.ofMillis(1), 2, Duration.ofSeconds(1), 1.5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
