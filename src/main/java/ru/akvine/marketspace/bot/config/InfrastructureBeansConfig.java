package ru.akvine.marketspace.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.impl.SessionStorageInMemoryImpl;
import ru.akvine.marketspace.bot.infrastructure.impl.StateStorageInMemoryImpl;

@Configuration
public class InfrastructureBeansConfig {
    @Bean
    public StateStorage<String> stateStorage() {
        return new StateStorageInMemoryImpl();
    }

    @Bean
    public SessionStorage<String, ClientSessionData> sessionStorage() {
        return new SessionStorageInMemoryImpl();
    }
}
