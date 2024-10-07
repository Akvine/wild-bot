package ru.akvine.wild.bot.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import ru.akvine.wild.bot.config.properties.RedisConfigProperties;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationServiceBuilder;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RedisConfigProperties.class)
@EnableCaching
public class RedisConfig {

    private static final String CACHE_PREFIX = "CACHE_";

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public RedisConnectionFactory connectionFactory(RedisConfigProperties redisConfigProperties) {
        if (redisConfigProperties.isClusterOn()) {
            RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(redisConfigProperties.getNodes());
            clusterConfiguration.setPassword(redisConfigProperties.getPassword());
            return new LettuceConnectionFactory(clusterConfiguration);
        } else {
            RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(redisConfigProperties.getHost(), redisConfigProperties.getPort());
            standaloneConfiguration.setPassword(redisConfigProperties.getPassword());
            return new LettuceConnectionFactory(standaloneConfiguration);
        }
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(CACHE_PREFIX)
                .entryTtl(Duration.ofMinutes(1L))
                .disableCachingNullValues();
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory, BatchStrategies.scan(1000)))
                .transactionAware()
                .cacheDefaults(defaults)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public RedissonClient redissonClient(RedisConfigProperties properties) {
        Config config = new Config();
        if (properties.isClusterOn()) {
            config.useClusterServers()
                    .addNodeAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                    .setPassword(properties.getPassword());
        } else {
            config.useSingleServer()
                    .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                    .setPassword(properties.getPassword());
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public <T> RedisOperationService<T> redisOperationService(RedissonClient redissonClient) {
        return new RedisOperationServiceBuilder<T>(redissonClient).build();
    }
}
