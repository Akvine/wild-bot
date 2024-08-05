package ru.akvine.marketspace.bot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.infrastructure.counter.CountersStorage;
import ru.akvine.marketspace.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.marketspace.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorageInDatabaseImpl;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorageInMemoryImpl;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorageInDatabaseImpl;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorageInMemoryImpl;
import ru.akvine.marketspace.bot.repositories.infrastructure.ClientSessionDataRepository;
import ru.akvine.marketspace.bot.repositories.infrastructure.IterationCounterRepository;
import ru.akvine.marketspace.bot.repositories.infrastructure.StateRepository;
import ru.akvine.marketspace.bot.services.AdvertService;

@Configuration
public class InfrastructureBeansConfig {
    @Bean
    public StateStorage<String> stateStorage() {
        return new StateStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "state.storage.implementation.type", havingValue = "memory")
    public StateStorage<String> memoryStateStorage() {
        return new StateStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "state.storage.implementation.type", havingValue = "database")
    public StateStorage<String> databaseStateStorage(StateRepository stateRepository) {
        return new StateStorageInDatabaseImpl(stateRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "memory")
    public SessionStorage<String, ClientSessionData> memorySessionStorage() {
        return new SessionStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "database")
    public SessionStorage<String, ClientSessionData> databaseSessionStorage(ClientSessionDataRepository clientSessionDataRepository) {
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

}
