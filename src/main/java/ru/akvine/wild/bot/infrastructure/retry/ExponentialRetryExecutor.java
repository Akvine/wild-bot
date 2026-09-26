package ru.akvine.wild.bot.infrastructure.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Реализация {@link RetryExecutor} с экспоненциально возрастающей задержкой.
 * <p>
 * Пауза после n-й неудачной попытки: {@code min(delay * multiplier^(n-1), maxDelay)}. Если задан
 * {@code jitterFactor}, пауза случайно отклоняется на ±{@code jitterFactor} (например, 0.2 - на ±20%),
 * чтобы клиенты, упавшие одновременно, не повторяли запросы одновременно; результат всё равно не
 * превышает {@code maxDelay}. Нулевая начальная задержка остаётся нулевой при любом множителе.
 */
public class ExponentialRetryExecutor extends AbstractRetryExecutor {
    private final double multiplier;
    private final Duration maxDelay;
    private final double jitterFactor;

    public ExponentialRetryExecutor(int attempts, Duration delay, double multiplier, Duration maxDelay) {
        this(attempts, delay, multiplier, maxDelay, 0.0);
    }

    public ExponentialRetryExecutor(
            int attempts, Duration delay, double multiplier, Duration maxDelay, double jitterFactor) {
        this(attempts, delay, multiplier, maxDelay, jitterFactor, duration -> Thread.sleep(duration.toMillis()));
    }

    public ExponentialRetryExecutor(
            int attempts, Duration delay, double multiplier, Duration maxDelay, double jitterFactor, Sleeper sleeper) {
        super(attempts, delay, sleeper);
        if (!(multiplier >= 1.0) || Double.isInfinite(multiplier)) {
            throw new IllegalArgumentException("multiplier must be a finite number >= 1 but was " + multiplier);
        }
        Objects.requireNonNull(maxDelay, "maxDelay");
        if (maxDelay.compareTo(delay) < 0) {
            throw new IllegalArgumentException("maxDelay [" + maxDelay + "] must be >= delay [" + delay + "]");
        }
        if (!(jitterFactor >= 0.0 && jitterFactor <= 1.0)) {
            throw new IllegalArgumentException("jitterFactor must be in [0, 1] but was " + jitterFactor);
        }
        this.multiplier = multiplier;
        this.maxDelay = maxDelay;
        this.jitterFactor = jitterFactor;
    }

    @Override
    protected Duration delayBeforeRetry(int failedAttempt) {
        double maxMillis = maxDelay.toMillis();
        double millis = Math.min(delay.toMillis() * Math.pow(multiplier, failedAttempt - 1), maxMillis);
        if (jitterFactor > 0) {
            double jitter = 1
                    - jitterFactor
                    + 2 * jitterFactor * ThreadLocalRandom.current().nextDouble();
            millis = Math.min(millis * jitter, maxMillis);
        }
        return Duration.ofMillis((long) millis);
    }
}
