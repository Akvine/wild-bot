package ru.akvine.wild.bot.infrastructure.retry;

import java.time.Duration;

/**
 * Реализация {@link RetryExecutor} с фиксированной задержкой между попытками: все повторы
 * выполняются через одинаковый промежуток времени, заданный при создании.
 */
public class DefaultRetryExecutor extends AbstractRetryExecutor {

    public DefaultRetryExecutor(int attempts, Duration delay) {
        super(attempts, delay);
    }

    public DefaultRetryExecutor(int attempts, Duration delay, Sleeper sleeper) {
        super(attempts, delay, sleeper);
    }

    @Override
    protected Duration delayBeforeRetry(int failedAttempt) {
        return delay;
    }
}
