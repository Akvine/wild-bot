package ru.akvine.wild.bot.infrastructure.retry;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Реализация {@link RetryExecutor} с фиксированной задержкой между попытками.
 * <p>
 * Все повторные попытки выполняются через одинаковый промежуток времени, заданный
 * при создании объекта. Если задача постоянно завершается исключением, после
 * исчерпания попыток будет выброшено последнее возникшее исключение (или исключение,
 * переданное в {@code throwIfAttemptsCountExceeded}, если попытки не выполнялись).
 */
public class DefaultRetryExecutor extends AbstractRetryExecutor {

    public DefaultRetryExecutor(int attempts, Duration delay) {
        super(attempts, delay);
    }

    @Override
    public void execute(Runnable task, RuntimeException throwIfAttemptsCountExceeded) {
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

        throw throwIfAttemptsCountExceeded;
    }

    @Override
    public <T> T execute(Supplier<T> task, RuntimeException throwIfAttemptsCountExceeded) {
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

        throw throwIfAttemptsCountExceeded;
    }
}
