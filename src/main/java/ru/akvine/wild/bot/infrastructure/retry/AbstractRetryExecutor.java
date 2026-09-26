package ru.akvine.wild.bot.infrastructure.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.exceptions.RetryException;

/**
 * Общий цикл повторов. Наследники задают только паузу перед очередной попыткой
 * ({@link #delayBeforeRetry(int)}).
 */
@Slf4j
public abstract class AbstractRetryExecutor implements RetryExecutor {

    /** Пауза между попытками; вынесена, чтобы в тестах не ждать по-настоящему */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    protected final int attempts;
    protected final Duration delay;
    private final Sleeper sleeper;

    protected AbstractRetryExecutor(int attempts, Duration delay) {
        this(attempts, delay, duration -> Thread.sleep(duration.toMillis()));
    }

    protected AbstractRetryExecutor(int attempts, Duration delay, Sleeper sleeper) {
        if (attempts < 1) {
            throw new IllegalArgumentException("attempts must be at least 1 but was " + attempts);
        }
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative but was " + delay);
        }
        this.attempts = attempts;
        this.delay = delay;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * @param failedAttempt номер только что неудавшейся попытки, начиная с 1
     * @return сколько ждать перед следующей попыткой
     */
    protected abstract Duration delayBeforeRetry(int failedAttempt);

    @Override
    public final <T> T call(Callable<T> task, Predicate<? super Exception> retryOn, String errorMessage) {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(retryOn, "retryOn");
        Objects.requireNonNull(errorMessage, "errorMessage");

        Exception lastException = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return task.call();
            } catch (Exception exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RetryException(
                            String.format("%s: interrupted on attempt %d/%d", errorMessage, attempt, attempts),
                            exception);
                }
                if (!retryOn.test(exception)) {
                    logger.warn(
                            "{}: attempt {}/{} failed with non-retryable {}: {}",
                            errorMessage,
                            attempt,
                            attempts,
                            exception.getClass().getSimpleName(),
                            exception.getMessage());
                    throw propagate(exception, errorMessage);
                }

                lastException = exception;
                if (attempt == attempts) {
                    break;
                }

                Duration pause = delayBeforeRetry(attempt);
                logger.warn(
                        "{}: attempt {}/{} failed with {}: {}. Retry in {} ms",
                        errorMessage,
                        attempt,
                        attempts,
                        exception.getClass().getSimpleName(),
                        exception.getMessage(),
                        pause.toMillis());
                try {
                    sleeper.sleep(pause);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    RetryException retryException = new RetryException(
                            String.format("%s: interrupted while waiting before retry", errorMessage),
                            interruptedException);
                    retryException.addSuppressed(exception);
                    throw retryException;
                }
            }
        }

        throw new RetryException(
                String.format("%s: attempts limit = [%d] exceeded", errorMessage, attempts), lastException);
    }

    private static RuntimeException propagate(Exception exception, String errorMessage) {
        return exception instanceof RuntimeException runtimeException
                ? runtimeException
                : new RetryException(errorMessage, exception);
    }
}
