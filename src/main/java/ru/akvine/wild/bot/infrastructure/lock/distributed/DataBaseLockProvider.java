package ru.akvine.wild.bot.infrastructure.lock.distributed;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.commons.cluster.lock.ConcurrentOperationsHelper;
import ru.akvine.commons.cluster.lock.SLock;
import ru.akvine.commons.cluster.lock.SLockProvider;
import ru.akvine.wild.bot.infrastructure.exceptions.DistributedLockAcquireException;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;

/**
 * Распределённая блокировка на основе БД ({@link SLockProvider}/{@link SLock}) — в отличие
 * от {@link ru.akvine.wild.bot.infrastructure.lock.InstanceLockProvider}, координирует
 * несколько инстансов приложения между собой. {@link #doWithLockAndTransaction} дополнительно
 * оборачивает переданную задачу в транзакцию, гарантированно снимая блокировку в {@code finally}
 * даже при ошибке коммита.
 */
@RequiredArgsConstructor
@Slf4j
public class DataBaseLockProvider implements DistributedLockProvider {
    private final ConcurrentOperationsHelper concurrentOperationsHelper;
    private final SLockProvider sLockProvider;
    private final TransactionTemplate transactionTemplate;

    public <T> T doWithLockAndTransaction(String lockId, Callable<T> job) {
        T result;

        SLock lock = sLockProvider.getLock(lockId);
        lock.lock();
        try {
            result = transactionTemplate.execute((status) -> {
                try {
                    return job.call();
                } catch (Exception ex) {
                    logger.debug("Error when performing job calling for lock {}", lockId, ex);
                    throw new RuntimeException(ex);
                }
            });
        } catch (RuntimeException wrapper) {
            Throwable cause = wrapper.getCause();
            if (cause != null) {
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
            }
            throw wrapper;
        } finally {
            try {
                lock.unlock();
            } catch (Exception ex) {
                logger.error("Error when performing unlock for lockId " + lockId, ex);
            }
        }

        return result;
    }

    @Override
    public <T> T lock(String lockId, Callable<T> job) {
        T result;

        try {
            result = concurrentOperationsHelper.doOnlineSyncOperation(job, lockId);
        } catch (RuntimeException wrapper) {
            Throwable cause = wrapper.getCause();
            if (cause != null) {
                throw (RuntimeException) cause;
            }

            throw wrapper;
        }

        return result;
    }

    @Override
    public <T> T lock(String lockId, Callable<T> job, long timeout, TimeUnit timeUnit) {
        T result;

        try {
            result = concurrentOperationsHelper.doSyncOperation(
                    job,
                    () -> {
                        throw new DistributedLockAcquireException("Lock acquire exception");
                    },
                    lockId,
                    timeout,
                    timeUnit);
        } catch (RuntimeException wrapper) {
            Throwable cause = wrapper.getCause();
            if (cause != null) {
                throw (RuntimeException) cause;
            }

            throw wrapper;
        }

        return result;
    }

    @Override
    public void lock(String lockId, Runnable job) {
        this.lock(lockId, () -> {
            job.run();
            return true;
        });
    }

    @Override
    public void lock(String lockId, Runnable job, long timeout, TimeUnit timeUnit) {
        this.lock(
                lockId,
                () -> {
                    job.run();
                    return true;
                },
                timeout,
                timeUnit);
    }

    @Override
    public boolean tryLock(String lockId, Runnable job) {
        return tryLock(lockId, job, 2, TimeUnit.MINUTES);
    }

    @Override
    public boolean tryLock(String lockId, Runnable job, long timeout, TimeUnit timeUnit) {
        SLock lock = sLockProvider.getLock(lockId);
        if (lock.isLocked()) {
            return false;
        }

        try {
            if (lock.tryLock(timeout, timeUnit)) {
                lock(
                        lockId,
                        () -> {
                            job.run();
                            return true;
                        },
                        timeout,
                        timeUnit);
                return true;
            }

            return false;
        } catch (InterruptedException exception) {
            String errorMessage = String.format("Interrupted while waiting for lock with key: %s", lockId);
            throw new DistributedLockAcquireException(errorMessage);
        }
    }

    @Override
    public void unlock(String key) {
        SLock lock = sLockProvider.getLock(key);
        lock.unlock();
    }
}
