package ru.akvine.marketspace.bot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.akvine.marketspace.bot.job.CheckRunningAdvertsJob;
import ru.akvine.marketspace.bot.job.SyncAdvertJob;
import ru.akvine.marketspace.bot.job.SyncCardJob;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.repositories.CardRepository;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.AdvertStatisticService;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.counter.IterationsCounterService;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;

@Configuration
@EnableScheduling
public class ScheduledConfig {

    @Bean
    @ConditionalOnProperty(name = "sync.enabled", havingValue = "true")
    public SyncAdvertJob syncAdvertJob(
            AdvertRepository advertRepository,
            AdvertService advertService,
            WildberriesIntegrationService wildberriesIntegrationService) {
        return new SyncAdvertJob(advertRepository, advertService, wildberriesIntegrationService);
    }

    @Bean
    @ConditionalOnProperty(name = "sync.enabled", havingValue = "true")
    public SyncCardJob syncCardJob(
            WildberriesIntegrationService wildberriesIntegrationService,
            CardRepository cardRepository,
            CardService cardService) {
        return new SyncCardJob(wildberriesIntegrationService, cardRepository, cardService);
    }

    @Bean
    public CheckRunningAdvertsJob checkRunningAdvertsJob(AdvertRepository advertRepository,
                                                         WildberriesIntegrationService wildberriesIntegrationService,
                                                         CardService cardService,
                                                         IterationsCounterService iterationsCounterService,
                                                         AdvertStatisticService advertStatisticService,
                                                         TelegramIntegrationService telegramIntegrationService) {
        return new CheckRunningAdvertsJob(
                advertRepository,
                cardService,
                telegramIntegrationService,
                wildberriesIntegrationService,
                iterationsCounterService,
                advertStatisticService
        );
    }
}
