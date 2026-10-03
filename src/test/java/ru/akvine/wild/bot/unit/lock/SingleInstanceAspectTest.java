package ru.akvine.wild.bot.unit.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.SingleInstance;
import ru.akvine.wild.bot.infrastructure.lock.SingleInstanceAspect;

class SingleInstanceAspectTest {
    private final FakeLockProvider lockProvider = new FakeLockProvider();
    private final Jobs target = new Jobs();
    private Jobs jobs;

    @BeforeEach
    void setUp() {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAspect(new SingleInstanceAspect(lockProvider));
        jobs = factory.getProxy();
    }

    @Test
    @DisplayName("Блокировка получена: джоб выполняется, lockId строится из класса и метода")
    void runsWhenLockAcquired() {
        jobs.work();

        assertThat(target.runs.get()).isEqualTo(1);
        assertThat(lockProvider.lastLockId).isEqualTo("JOB_Jobs_work");
    }

    @Test
    @DisplayName("Блокировка занята другим экземпляром: запуск пропускается без ошибки")
    void skipsWhenLockHeld() {
        lockProvider.available = false;

        jobs.work();

        assertThat(target.runs.get()).isZero();
    }

    @Test
    @DisplayName("Явный lockId из аннотации имеет приоритет")
    void explicitLockId() {
        jobs.named();

        assertThat(lockProvider.lastLockId).isEqualTo("custom-lock");
    }

    @Test
    @DisplayName("Исключение джоба пробрасывается как есть, в том числе проверяемое")
    void rethrowsOriginalException() {
        assertThatThrownBy(() -> jobs.failChecked())
                .isInstanceOf(IOException.class)
                .hasMessage("boom");
        assertThatThrownBy(() -> jobs.failUnchecked())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("bad");
    }

    @Test
    @DisplayName("Метод не void: понятная ошибка вместо тихого null")
    void nonVoidRejected() {
        assertThatThrownBy(() -> jobs.value()).isInstanceOf(IllegalStateException.class);
    }

    public static class Jobs {
        final AtomicInteger runs = new AtomicInteger();

        @SingleInstance
        public void work() {
            runs.incrementAndGet();
        }

        @SingleInstance("custom-lock")
        public void named() {}

        @SingleInstance
        public void failChecked() throws IOException {
            throw new IOException("boom");
        }

        @SingleInstance
        public void failUnchecked() {
            throw new IllegalStateException("bad");
        }

        @SingleInstance
        public int value() {
            return 1;
        }
    }

    static class FakeLockProvider implements DistributedLockProvider {
        boolean available = true;
        String lastLockId;

        @Override
        public boolean tryLock(String lockId, Runnable job) {
            lastLockId = lockId;
            if (!available) {
                return false;
            }
            job.run();
            return true;
        }

        @Override
        public boolean tryLock(String lockId, Runnable job, long timeout, TimeUnit timeUnit) {
            return tryLock(lockId, job);
        }

        @Override
        public <T> T lock(String lockId, Callable<T> job) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T lock(String lockId, Callable<T> job, long timeout, TimeUnit timeUnit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lock(String lockId, Runnable runnable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lock(String lockId, Runnable runnable, long timeout, TimeUnit timeUnit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unlock(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLocked(String key) {
            throw new UnsupportedOperationException();
        }
    }
}
