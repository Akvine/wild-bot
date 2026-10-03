package ru.akvine.wild.bot.infrastructure.lock;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Реализация {@link SingleInstance}. Порядок выше, чем у транзакционного advisor'а: сначала берётся блокировка и только
 * потом открывается транзакция метода (например, у {@code SubscriptionJob}), а не наоборот.
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SingleInstanceAspect {
    private static final String LOCK_PREFIX = "JOB_";

    private final DistributedLockProvider lockProvider;

    public SingleInstanceAspect(DistributedLockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    @Around("@annotation(singleInstance)")
    public Object runOnSingleInstance(ProceedingJoinPoint joinPoint, SingleInstance singleInstance) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (signature.getReturnType() != void.class) {
            // при пропуске запуска возвращать было бы нечего
            throw new IllegalStateException(
                    "@SingleInstance supports only void methods, but " + signature.toShortString() + " returns "
                            + signature.getReturnType().getSimpleName());
        }

        String lockId = StringUtils.hasText(singleInstance.value())
                ? singleInstance.value()
                : LOCK_PREFIX + signature.getDeclaringType().getSimpleName() + "_" + signature.getName();

        boolean executed;
        try {
            executed = lockProvider.tryLock(lockId, () -> {
                try {
                    joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new JobFailure(throwable);
                }
            });
        } catch (JobFailure failure) {
            throw failure.getCause();
        }

        if (!executed) {
            logger.debug(
                    "Job [{}] is skipped: lock is held by another instance or the previous run is still going", lockId);
        }
        return null;
    }

    /** Переносит проверяемое исключение джоба через {@link Runnable}, не меняя его тип для вызывающего */
    private static final class JobFailure extends RuntimeException {
        JobFailure(Throwable cause) {
            super(cause);
        }
    }
}
