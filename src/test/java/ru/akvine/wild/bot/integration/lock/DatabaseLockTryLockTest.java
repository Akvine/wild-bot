package ru.akvine.wild.bot.integration.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.utils.UUIDGenerator;

/**
 * {@code tryLock} - основа выбора единственного исполнителя фоновых джобов (@SingleInstance), поэтому его контракт
 * из javadoc {@link DistributedLockProvider} проверяется на настоящей БД-блокировке: не ждёт, выполняет задачу,
 * гарантированно освобождает блокировку.
 */
class DatabaseLockTryLockTest extends BaseTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @Autowired
    private DistributedLockProvider lockProvider;

    private String lockId() {
        return "TRY_LOCK_TEST_" + UUIDGenerator.uuidWithoutDashes();
    }

    @Test
    void tryLock_runsJobAndReleasesLock() {
        String lockId = lockId();
        AtomicInteger runs = new AtomicInteger();

        boolean executed =
                assertTimeoutPreemptively(TIMEOUT, () -> lockProvider.tryLock(lockId, runs::incrementAndGet));

        assertThat(executed).isTrue();
        assertThat(runs.get()).isEqualTo(1);
        assertThat(lockProvider.isLocked(lockId)).isFalse();
        // освобождённую блокировку можно взять снова
        assertThat(assertTimeoutPreemptively(TIMEOUT, () -> lockProvider.tryLock(lockId, runs::incrementAndGet)))
                .isTrue();
        assertThat(runs.get()).isEqualTo(2);
    }

    @Test
    void tryLock_whenLockIsHeldByAnotherThread_returnsFalseImmediatelyWithoutRunningJob() throws Exception {
        String lockId = lockId();
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService holder = Executors.newSingleThreadExecutor();
        try {
            Future<?> holding = holder.submit(() -> lockProvider.lock(lockId, () -> {
                held.countDown();
                try {
                    release.await(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            assertThat(held.await(10, TimeUnit.SECONDS)).isTrue();

            AtomicInteger runs = new AtomicInteger();
            long start = System.nanoTime();
            boolean executed =
                    assertTimeoutPreemptively(TIMEOUT, () -> lockProvider.tryLock(lockId, runs::incrementAndGet));
            long millis = (System.nanoTime() - start) / 1_000_000;

            assertThat(executed).isFalse();
            assertThat(runs.get()).isZero();
            assertThat(millis)
                    .as("tryLock без таймаута не должен ждать освобождения")
                    .isLessThan(5_000);

            release.countDown();
            holding.get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            holder.shutdownNow();
        }
    }

    @Test
    void tryLock_whenJobFails_propagatesExceptionAndReleasesLock() {
        String lockId = lockId();

        assertThatThrownBy(() -> assertTimeoutPreemptively(
                        TIMEOUT,
                        () -> lockProvider.tryLock(lockId, () -> {
                            throw new IllegalStateException("boom");
                        })))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        assertThat(lockProvider.isLocked(lockId)).isFalse();
    }
}
