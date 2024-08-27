package ru.akvine.wild.bot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInMemoryImpl;
import ru.akvine.wild.bot.repositories.infrastructure.ClientSessionDataRepository;
import ru.akvine.wild.bot.repositories.infrastructure.IterationCounterRepository;
import ru.akvine.wild.bot.services.AdvertService;

import java.util.List;

@Configuration
public class InfrastructureBeansConfig {

    @Bean
    public StateStorage<String, List<ClientState>> memoryStateStorage() {
        return new StateStorageInMemoryImpl();
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
