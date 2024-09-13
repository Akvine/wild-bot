package ru.akvine.wild.bot.infrastructure.lock;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Следует использовать только если в production-среде не более 1 инстанса приложения.
 * В противном случае стоит использовать RedisInMemoryLockProvider или DataBaseLockProvider
 */
@Component
public class InstanceLockProvider implements InMemoryLockProvider {
    private static final Map<String, LockWrapper> locks = new ConcurrentHashMap<>();

    @Override
    public void lock(String key) {
        LockWrapper lockWrapper = locks.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
        lockWrapper.lock.lock();
    }

    @Override
    public boolean tryLock(String key) {
        lock(key);
        return true;
    }

    @Override
    public void unlock(String key) {
        LockWrapper lockWrapper = locks.get(key);
        lockWrapper.lock.unlock();
        if (lockWrapper.removeThreadFromQueue() == 0) {
            locks.remove(key, lockWrapper);
        }

    }

    private static class LockWrapper {
        private final Lock lock = new ReentrantLock();
        private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

        private LockWrapper addThreadInQueue() {
            numberOfThreadsInQueue.incrementAndGet();
            return this;
        }

        private int removeThreadFromQueue() {
            return numberOfThreadsInQueue.decrementAndGet();
        }
    }
}
