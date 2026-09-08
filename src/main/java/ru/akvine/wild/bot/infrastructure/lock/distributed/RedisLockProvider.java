package ru.akvine.wild.bot.infrastructure.lock.distributed;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import ru.akvine.wild.bot.infrastructure.exceptions.DistributedLockAcquireException;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;

/**
 * Реализация {@link DistributedLockProvider} поверх Redis (Redisson {@code RLock}, справедливая
 * блокировка). Включается только при {@code spring.redis.enabled=true}.
 */
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
@Slf4j
public class RedisLockProvider implements DistributedLockProvider {
    private final RedissonClient redisson;

    @Override
    public <T> T lock(String lockId, Callable<T> job) {
        return lock(lockId, job, 2, TimeUnit.MINUTES);
    }

    @Override
    public <T> T lock(String lockId, Callable<T> job, long timeout, TimeUnit timeUnit) {
        RLock lock = redisson.getFairLock(lockId);
        try {
            if (lock.tryLock(timeout, timeUnit)) {
                return launchAndUnlock(lockId, lock, job);
            } else {
                String errorMessage = String.format("Attempt to get lock for key [%s] failed with timeout", lockId);
                throw new DistributedLockAcquireException(errorMessage);
            }
        } catch (InterruptedException interruptedException) {
            String errorMessage = String.format("Interrupted while waiting for lock with key: %s", lockId);
            throw new DistributedLockAcquireException(errorMessage);
        }
    }

    @Override
    public void lock(String lockId, Runnable runnable) {
        lock(lockId, () -> {
            runnable.run();
            return true;
        });
    }

    @Override
    public void lock(String lockId, Runnable runnable, long timeout, TimeUnit timeUnit) {
        lock(
                lockId,
                () -> {
                    runnable.run();
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
        RLock lock = redisson.getLock(lockId);

        try {
            if (lock.tryLock(timeout, timeUnit)) {
                return launchAndUnlock(lockId, lock, () -> {
                    job.run();
                    return null;
                });
            } else {
                return false;
            }
        } catch (InterruptedException interruptedException) {
            String errorMessage = String.format("Interrupted while waiting for lock with key: %s", lockId);
            throw new DistributedLockAcquireException(errorMessage);
        }
    }

    @Override
    public void unlock(String key) {
        RLock fairLock = redisson.getFairLock(key);
        fairLock.unlock();
    }

    private <T> T launchAndUnlock(String lockId, RLock rLock, Callable<T> job) {
        try {
            return job.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rLock.unlock();
            } catch (Exception e) {
                logger.error("Error when performing unlock for lockId " + lockId, e);
            }
        }
    }
}
