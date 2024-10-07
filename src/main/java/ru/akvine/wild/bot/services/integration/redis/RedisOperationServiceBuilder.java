package ru.akvine.wild.bot.services.integration.redis;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;

@RequiredArgsConstructor
public class RedisOperationServiceBuilder<T> {
    private final RedissonClient redissonClient;

    public RedisOperationService<T> build() {
        return new RedisOperationService<>(redissonClient);
    }
}
