package ru.akvine.wild.bot.infrastructure.retry;

import java.time.Duration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractRetryExecutor implements RetryExecutor {
    // TODO: сделать RetryExecutor еще и распределенным за счет доп. параметра distributedLockId (String)
    protected final int attempts;
    protected final Duration delay;
}
