package ru.akvine.wild.bot.integration.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.infrastructure.exceptions.DistributedLockAcquireException;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.DataBaseLockProvider;
import ru.akvine.wild.bot.integration.base.BaseTest;

@DisplayName("DataBaseLockProvider: блокирующие варианты lock, unlock, isLocked и работа в транзакции")
class DatabaseLockProviderTest extends BaseTest {
    @Autowired
    private DistributedLockProvider provider;

    private static String id() {
        return "TEST_LOCK_" + UUID.randomUUID();
    }

    @Test
    @DisplayName("lock(Callable) возвращает результат, блокировка внутри задачи занята, после неё свободна")
    void lockReturnsResult() {
        String lockId = id();

        boolean lockedInside = provider.lock(lockId, () -> provider.isLocked(lockId));

        assertThat(lockedInside).isTrue();
        assertThat(provider.isLocked(lockId)).isFalse();
    }

    @Test
    @DisplayName("lock(Runnable) и варианты с таймаутом выполняют задачу")
    void lockRunnableAndTimeout() {
        AtomicInteger runs = new AtomicInteger();

        provider.lock(id(), runs::incrementAndGet);
        provider.lock(id(), runs::incrementAndGet, 5, TimeUnit.SECONDS);
        assertThat(provider.lock(id(), () -> "value", 5, TimeUnit.SECONDS)).isEqualTo("value");

        assertThat(runs.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Исключение задачи пробрасывается без обёртки, блокировка при этом освобождается")
    void jobExceptionIsUnwrappedAndLockReleased() {
        String lockId = id();

        assertThatThrownBy(() -> provider.lock(lockId, () -> {
                    throw new IllegalStateException("boom");
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
        assertThatThrownBy(() -> provider.lock(lockId, () -> {
                    throw new IOException("checked");
                }))
                .isInstanceOf(RuntimeException.class);

        assertThat(provider.isLocked(lockId)).isFalse();
        assertThat(provider.lock(lockId, () -> "again")).isEqualTo("again");
    }

    @Test
    @DisplayName("Занятая другим потоком блокировка не берётся за короткий таймаут")
    void lockTimesOutWhenHeldElsewhere() throws Exception {
        String lockId = id();
        CountDownLatch acquired = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> holder = executor.submit(() -> provider.lock(lockId, () -> {
                acquired.countDown();
                try {
                    release.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            assertThat(acquired.await(10, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> provider.lock(lockId, () -> "x", 200, TimeUnit.MILLISECONDS))
                    .isInstanceOf(DistributedLockAcquireException.class);

            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("doWithLockAndTransaction выполняет задачу под блокировкой в транзакции и разворачивает ошибки")
    void doWithLockAndTransaction() {
        DataBaseLockProvider database = (DataBaseLockProvider) provider;
        String lockId = id();

        assertThat(database.doWithLockAndTransaction(lockId, () -> 5)).isEqualTo(5);
        assertThatThrownBy(() -> database.doWithLockAndTransaction(lockId, () -> {
                    throw new IllegalArgumentException("bad");
                }))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> database.doWithLockAndTransaction(lockId, () -> {
                    throw new IOException("checked");
                }))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
        assertThat(provider.isLocked(lockId)).isFalse();
    }

    @Test
    @DisplayName("Ручной unlock чужой или несуществующей блокировки не ломает проверку isLocked")
    void manualUnlock() {
        String lockId = id();
        provider.lock(lockId, () -> provider.isLocked(lockId));

        assertThat(provider.isLocked(lockId)).isFalse();
    }
}
