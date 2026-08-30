package ru.akvine.wild.bot.helpers;

import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class RetryHelper {
    public <T> T retry(int attempts, Duration delay, Supplier<T> task, RuntimeException exceptionToThrowIfFailed) {
        for (int i = 1; i <= attempts; i++) {
            try {
                return task.get();
            } catch (Exception exception) {
                if (i == attempts) {
                    throw exception;
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw exceptionToThrowIfFailed;
    }

    public void retryWithoutResult(
            int attempts, Duration delay, Runnable task, RuntimeException exceptionToThrowIfFailed) {
        for (int i = 1; i <= attempts; i++) {
            try {
                task.run();
            } catch (Exception exception) {
                if (i == attempts) {
                    throw exception;
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw exceptionToThrowIfFailed;
    }

    public <T> T retryWithExponentialBackoff(
            int attempts,
            Duration initialDelay,
            double multiplier,
            Duration maxDelay,
            Supplier<T> task,
            RuntimeException exceptionToThrowIfFailed) {

        Duration currentDelay = initialDelay;
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
        throw exceptionToThrowIfFailed;
    }

    public void retryWithExponentialBackoffWithoutResult(
            int attempts,
            Duration initialDelay,
            double multiplier,
            Duration maxDelay,
            Runnable task,
            RuntimeException exceptionToThrowIfFailed) {

        retryWithExponentialBackoff(
                attempts,
                initialDelay,
                multiplier,
                maxDelay,
                () -> {
                    task.run();
                    return null;
                },
                exceptionToThrowIfFailed);
    }
}
