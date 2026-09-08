package ru.akvine.wild.bot.infrastructure.retry;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Реализация {@link RetryExecutor} с экспоненциально возрастающей задержкой.
 * <p>
 * Задержка между попытками увеличивается по формуле:
 * {@code текущаяЗадержка = min(предыдущаяЗадержка * multiplier, maxDelay)}.
 * Первая задержка равна базовой, заданной при создании.
 */
public class ExponentialRetryExecutor extends AbstractRetryExecutor {
    private final double multiplier;
    private final Duration maxDelay;

    public ExponentialRetryExecutor(int attempts, Duration delay, double multiplier, Duration maxDelay) {
        super(attempts, delay);
        this.multiplier = multiplier;
        this.maxDelay = maxDelay;
    }

    @Override
    public void execute(Runnable task, RuntimeException throwIfAttemptsCountExceeded) {
        execute(
                () -> {
                    task.run();
                    return null;
                },
                throwIfAttemptsCountExceeded);
    }

    @Override
    public <T> T execute(Supplier<T> task, RuntimeException throwIfAttemptsCountExceeded) {
        Duration currentDelay = delay;
        for (int i = 1; i <= attempts; i++) {
            try {
                return task.get();
            } catch (Exception exception) {
                if (i == attempts) {
                    throw exception;
                }
                try {
                    Thread.sleep(currentDelay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted during retry backoff", ie);
                }

                // Увеличиваем задержку для следующей попытки
                currentDelay =
                        Duration.ofMillis(Math.min((long) (currentDelay.toMillis() * multiplier), maxDelay.toMillis()));
            }
        }
        throw throwIfAttemptsCountExceeded;
    }
}
