package ru.akvine.marketspace.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.enums.IterationsCounterServiceType;
import ru.akvine.marketspace.bot.repositories.IterationCounterRepository;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.counter.IterationsCounterService;
import ru.akvine.marketspace.bot.services.counter.IterationsCounterServiceInDatabaseImpl;
import ru.akvine.marketspace.bot.services.counter.IterationsCounterServiceInMemoryImpl;

@Configuration
public class IterationsCounterConfig {
    @Value("${iterations.counter.service.type}")
    private String type;

    @Bean
    public IterationsCounterService iterationsCounterService(AdvertService advertService,
                                                             IterationCounterRepository iterationCounterRepository) {
        IterationsCounterServiceType counterServiceType = IterationsCounterServiceType.safeValueOf(type);

        if (counterServiceType == IterationsCounterServiceType.IN_MEMORY) {
            return new IterationsCounterServiceInMemoryImpl(advertService);
        } else {
            return new IterationsCounterServiceInDatabaseImpl(iterationCounterRepository);
        }
    }
}
