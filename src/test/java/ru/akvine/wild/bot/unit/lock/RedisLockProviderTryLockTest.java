package ru.akvine.wild.bot.unit.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import ru.akvine.wild.bot.infrastructure.lock.distributed.RedisLockProvider;

class RedisLockProviderTryLockTest {
    private final RedissonClient redisson = mock(RedissonClient.class);
    private final RLock rLock = mock(RLock.class);
    private RedisLockProvider provider;

    @BeforeEach
    void setUp() {
        when(redisson.getLock("job")).thenReturn(rLock);
        provider = new RedisLockProvider(redisson);
    }

    @Test
    @DisplayName("Блокировка свободна: задача выполняется, результат true, блокировка снимается")
    void runsJobAndReturnsTrue() throws Exception {
        when(rLock.tryLock(anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(true);
        AtomicInteger runs = new AtomicInteger();

        boolean executed = provider.tryLock("job", runs::incrementAndGet);

        assertThat(executed).isTrue();
        assertThat(runs.get()).isEqualTo(1);
        verify(rLock).unlock();
    }

    @Test
    @DisplayName("tryLock без таймаута не ждёт блокировку (waitTime = 0)")
    void doesNotWaitWithoutTimeout() throws Exception {
        when(rLock.tryLock(anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(false);

        provider.tryLock("job", () -> {});

        verify(rLock).tryLock(0L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("Блокировка занята: false, задача не выполняется, unlock не вызывается")
    void skipsJobWhenLockIsBusy() throws Exception {
        when(rLock.tryLock(anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(false);
        AtomicInteger runs = new AtomicInteger();

        boolean executed = provider.tryLock("job", runs::incrementAndGet);

        assertThat(executed).isFalse();
        assertThat(runs.get()).isZero();
        verify(rLock, never()).unlock();
    }

    @Test
    @DisplayName("Ошибка задачи пробрасывается, блокировка всё равно снимается")
    void releasesLockWhenJobFails() throws Exception {
        when(rLock.tryLock(anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenReturn(true);

        assertThatThrownBy(() -> provider.tryLock("job", () -> {
                    throw new IllegalStateException("boom");
                }))
                .isInstanceOf(IllegalStateException.class);

        verify(rLock).unlock();
    }
}
