package ru.akvine.wild.bot.unit.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import ru.akvine.wild.bot.infrastructure.exceptions.DistributedLockAcquireException;
import ru.akvine.wild.bot.infrastructure.lock.distributed.RedisLockProvider;

@DisplayName("RedisLockProvider: блокирующие варианты lock, unlock и isLocked")
class RedisLockProviderLockTest {
    private final RedissonClient redisson = mock(RedissonClient.class);
    private final RLock fairLock = mock(RLock.class);
    private final RLock lock = mock(RLock.class);
    private RedisLockProvider provider;

    @BeforeEach
    void setUp() {
        when(redisson.getFairLock("job")).thenReturn(fairLock);
        when(redisson.getLock("job")).thenReturn(lock);
        provider = new RedisLockProvider(redisson);
    }

    @Test
    @DisplayName("lock(Callable): берёт справедливую блокировку на 2 минуты, возвращает результат и снимает блокировку")
    void lockReturnsResultAndUnlocks() throws Exception {
        when(fairLock.tryLock(2, TimeUnit.MINUTES)).thenReturn(true);

        String result = provider.lock("job", () -> "done");

        assertThat(result).isEqualTo("done");
        verify(fairLock).unlock();
    }

    @Test
    @DisplayName("lock(Runnable) и варианты с таймаутом выполняют задачу под блокировкой")
    void lockRunnableVariants() throws Exception {
        when(fairLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
        AtomicInteger runs = new AtomicInteger();

        provider.lock("job", runs::incrementAndGet);
        provider.lock("job", runs::incrementAndGet, 5, TimeUnit.SECONDS);
        provider.lock("job", () -> runs.incrementAndGet(), 1, TimeUnit.SECONDS);

        assertThat(runs.get()).isEqualTo(3);
        verify(fairLock).tryLock(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Блокировка не получена за таймаут - DistributedLockAcquireException, задача не выполняется")
    void lockTimeout() throws Exception {
        when(fairLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(false);
        AtomicInteger runs = new AtomicInteger();

        assertThatThrownBy(() -> provider.lock("job", runs::incrementAndGet, 1, TimeUnit.SECONDS))
                .isInstanceOf(DistributedLockAcquireException.class);

        assertThat(runs.get()).isZero();
        verify(fairLock, never()).unlock();
    }

    @Test
    @DisplayName("Прерывание при ожидании блокировки - DistributedLockAcquireException")
    void lockInterrupted() throws Exception {
        when(fairLock.tryLock(1, TimeUnit.SECONDS)).thenThrow(new InterruptedException("stop"));

        assertThatThrownBy(() -> provider.lock("job", () -> "x", 1, TimeUnit.SECONDS))
                .isInstanceOf(DistributedLockAcquireException.class);
    }

    @Test
    @DisplayName(
            "Исключения задачи: непроверяемое пробрасывается, проверяемое оборачивается; блокировка снимается всегда")
    void jobFailuresStillUnlock() throws Exception {
        when(fairLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);

        assertThatThrownBy(() -> provider.lock("job", () -> {
                    throw new IllegalStateException("boom");
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
        assertThatThrownBy(() -> provider.lock("job", () -> {
                    throw new IOException("checked");
                }))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);

        verify(fairLock, org.mockito.Mockito.times(2)).unlock();
    }

    @Test
    @DisplayName("Ошибка снятия блокировки не скрывает результат задачи")
    void unlockFailureIsSwallowed() throws Exception {
        when(fairLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
        doThrow(new IllegalMonitorStateException("not owner")).when(fairLock).unlock();

        assertThat(provider.lock("job", () -> 7)).isEqualTo(7);
    }

    @Test
    @DisplayName("unlock снимает справедливую блокировку, isLocked спрашивает обычную")
    void unlockAndIsLocked() {
        when(lock.isLocked()).thenReturn(true);

        provider.unlock("job");

        verify(fairLock).unlock();
        assertThat(provider.isLocked("job")).isTrue();
    }
}
