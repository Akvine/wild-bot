package ru.akvine.wild.bot.config;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.retry.DefaultRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.ExponentialRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.RetryExecutor;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInMemoryImpl;
import ru.akvine.wild.bot.repositories.infrastructure.ClientSessionDataRepository;
import ru.akvine.wild.bot.repositories.infrastructure.ClientStatesRepository;
import ru.akvine.wild.bot.repositories.infrastructure.IterationCounterRepository;
import ru.akvine.wild.bot.services.AdvertService;

@Configuration
public class InfrastructureBeansConfig {

    @Bean
    @ConditionalOnProperty(name = "states.storage.implementation.type", havingValue = "memory")
    public StateStorage<String, List<ClientState>> memoryStateStorage() {
        return new StateStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "states.storage.implementation.type", havingValue = "database")
    public StateStorage<String, List<ClientState>> databaseStateStorage(ClientStatesRepository clientStatesRepository) {
        return new StateStorageInDatabaseImpl(clientStatesRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "memory")
    public SessionStorage<String, ClientSessionData> memorySessionStorage() {
        return new SessionStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "database")
    public SessionStorage<String, ClientSessionData> databaseSessionStorage(
            ClientSessionDataRepository clientSessionDataRepository) {
        return new SessionStorageInDatabaseImpl(clientSessionDataRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "counter.storage.implementation.type", havingValue = "memory")
    public CountersStorage memoryIterationsStorage(AdvertService advertService) {
        return new CountersStorageInMemoryImpl(advertService);
    }

    @Bean
    @ConditionalOnProperty(name = "counter.storage.implementation.type", havingValue = "database")
    public CountersStorage databaseIterationsStorage(IterationCounterRepository iterationCounterRepository) {
        return new CountersStorageInDatabaseImpl(iterationCounterRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "retry.executor.implementation.type", havingValue = "exponential")
    public RetryExecutor exponentialRetryExecutor(
            @Value("${send.file.retry.attempts.count}") int attempts,
            @Value("${send.file.retry.initial.delay.millis}") int retryInitialDelayMillis,
            @Value("${send.file.retry.exponential.backoff.multiplier}") double retryExponentialBackoffMultiplier,
            @Value("${send.file.retry.max.delay.millis}") int retryMaxDelayMillis) {

        return new ExponentialRetryExecutor(
                attempts,
                Duration.ofMillis(retryInitialDelayMillis),
                retryExponentialBackoffMultiplier,
                Duration.ofMillis(retryMaxDelayMillis));
    }

    @Bean
    @ConditionalOnProperty(name = "retry.executor.implementation.type", havingValue = "default")
    public RetryExecutor defaultRetryExecutor(
            @Value("${send.file.retry.attempts.count}") int attempts,
            @Value("${send.file.retry.initial.delay.millis}") int retryInitialDelayMillis) {
        return new DefaultRetryExecutor(attempts, Duration.ofMillis(retryInitialDelayMillis));
    }
}
