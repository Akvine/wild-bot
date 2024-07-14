package ru.akvine.marketspace.bot.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.commons.cluster.lock.ConcurrentOperationsHelper;
import ru.akvine.commons.cluster.lock.SLock;
import ru.akvine.commons.cluster.lock.SLockProvider;

import java.util.concurrent.Callable;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockHelper {
    private final ConcurrentOperationsHelper concurrentOperationsHelper;
    private final SLockProvider sLockProvider;
    private final TransactionTemplate transactionTemplate;

    public <T> T doWithLock(String lockId, Callable<T> job) {
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
    public void doWithLock(String lockId, Runnable job) {
        this.doWithLock(lockId, () -> {
            job.run();
            return true;
        });
    }

    public <T> T doWithLockAndTransaction(String lockId, Callable<T> job) {
        T result;

        SLock lock = sLockProvider.getLock(lockId);
        lock.lock();
        try {
            result = transactionTemplate.execute((status) -> {
                try {
                    return job.call();
                } catch (Exception ex) {
                    logger.debug("Error when performing job calling for lock " + lockId, ex);
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

}
