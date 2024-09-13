package ru.akvine.wild.bot.infrastructure.lock.distributed;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.infrastructure.lock.InMemoryLockProvider;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
public class RedisInMemoryLockProvider implements InMemoryLockProvider {
    private final RedissonClient redisson;

    @Override
    public void lock(String key) {
        RLock fairLock =  redisson.getFairLock(key);
        fairLock.lock();
    }

    @Override
    public boolean tryLock(String key) {
        RLock fairLock =  redisson.getFairLock(key);
        return fairLock.tryLock();
    }

    @Override
    public void unlock(String key) {
        RLock fairLock =  redisson.getFairLock(key);
        fairLock.unlock();
    }
}
