package ru.akvine.wild.bot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.job.CheckRunningAdvertsJob;
import ru.akvine.wild.bot.job.SubscriptionJob;
import ru.akvine.wild.bot.job.sync.GlobalSyncJob;
import ru.akvine.wild.bot.job.sync.SyncAdvertJob;
import ru.akvine.wild.bot.job.sync.SyncCardJob;
import ru.akvine.wild.bot.job.sync.SyncCardTypeJob;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;

@Configuration
@EnableScheduling
public class ScheduledConfig {
    private final static String SYSTEM = "system";

    @Bean
    @ConditionalOnProperty(name = "global.sync.enabled", havingValue = "true")
    public GlobalSyncJob globalSyncJob(SyncCardTypeJob syncCardTypeJob,
                                       SyncCardJob syncCardJob,
                                       SyncAdvertJob syncAdvertJob) {
        return new GlobalSyncJob(
                syncCardTypeJob,
                syncCardJob,
                syncAdvertJob,
                GlobalSyncJob.class.getSimpleName(),
                SYSTEM);
    }

    @Bean
    public CheckRunningAdvertsJob checkRunningAdvertsJob(AdvertRepository advertRepository,
                                                         WildberriesIntegrationService wildberriesIntegrationService,
                                                         CountersStorage countersStorage,
                                                         AdvertStatisticService advertStatisticService,
                                                         TelegramIntegrationService telegramIntegrationService) {
        return new CheckRunningAdvertsJob(
                advertRepository,
                telegramIntegrationService,
                wildberriesIntegrationService,
                countersStorage,
                advertStatisticService,
                CheckRunningAdvertsJob.class.getSimpleName(),
                SYSTEM
        );
    }

    @Bean
    public SubscriptionJob subscriptionJob(TelegramIntegrationService telegramIntegrationService,
                                           SubscriptionRepository subscriptionRepository) {
        return new SubscriptionJob(
                telegramIntegrationService,
                subscriptionRepository,
                SubscriptionJob.class.getSimpleName(),
                SYSTEM
        );
    }
}
