package ru.akvine.wild.bot.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.property.printers.PropertiesPrinter;
import ru.akvine.wild.bot.job.CheckRunningAdvertsJob;
import ru.akvine.wild.bot.job.PrintPropertiesJob;
import ru.akvine.wild.bot.job.SubscriptionJob;
import ru.akvine.wild.bot.job.domain.DeleteAdvertsAndStatisticsJob;
import ru.akvine.wild.bot.job.monitoring.HikariPoolMetricsJob;
import ru.akvine.wild.bot.job.sync.*;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;
import ru.akvine.wild.bot.services.integration.custodian.CustodianIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.property.PropertyService;
import ru.akvine.wild.bot.services.property.PropertyServiceImpl;

@Configuration
@EnableScheduling
public class ScheduledConfig {
    private static final String SYSTEM = "system";

    @Bean
    @ConditionalOnProperty(name = "global.sync.enabled", havingValue = "true")
    public GlobalSyncJob globalSyncJob(
            SyncCardTypeJob syncCardTypeJob, SyncCardJob syncCardJob, SyncAdvertJob syncAdvertJob) {
        return new GlobalSyncJob(
                syncCardTypeJob, syncCardJob, syncAdvertJob, GlobalSyncJob.class.getSimpleName(), SYSTEM);
    }

    @Bean
    public CheckRunningAdvertsJob checkRunningAdvertsJob(
            AdvertRepository advertRepository,
            WildberriesIntegrationService wildberriesIntegrationService,
            CountersStorage countersStorage,
            AdvertStatisticService advertStatisticService,
            BotIntegrationAdapter botIntegrationAdapter,
            PropertyService propertyService) {
        return new CheckRunningAdvertsJob(
                advertRepository,
                botIntegrationAdapter,
                wildberriesIntegrationService,
                countersStorage,
                advertStatisticService,
                propertyService,
                CheckRunningAdvertsJob.class.getSimpleName(),
                SYSTEM,
                SYSTEM);
    }

    @Bean
    public SubscriptionJob subscriptionJob(
            BotIntegrationAdapter botIntegrationAdapter, SubscriptionRepository subscriptionRepository) {
        return new SubscriptionJob(
                botIntegrationAdapter, subscriptionRepository, SubscriptionJob.class.getSimpleName(), SYSTEM);
    }

    @Bean
    @ConditionalOnProperty(name = "custodian.integration.enabled", havingValue = "true")
    public SyncPropertiesJob syncPropertiesJob(
            PropertyService propertyService, CustodianIntegrationService custodianIntegrationService) {
        return new SyncPropertiesJob(propertyService, custodianIntegrationService);
    }

    @Bean
    @ConditionalOnProperty(name = "print.properties.enabled", havingValue = "true")
    public PrintPropertiesJob printPropertiesJob(
            PropertyServiceImpl propertyServiceImpl, PropertiesPrinter propertiesPrinter) {
        return new PrintPropertiesJob(propertyServiceImpl, propertiesPrinter);
    }

    @Bean
    @ConditionalOnProperty(name = "hikari.pool.metrics.log.enabled", havingValue = "true")
    public HikariPoolMetricsJob hikariPoolMetricsJob(HikariDataSource hikariDataSource) {
        return new HikariPoolMetricsJob(hikariDataSource);
    }

    @Bean
    public DeleteAdvertsAndStatisticsJob deleteAdvertsAndStatisticsJob(
            AdvertStatisticRepository statisticRepository, AdvertRepository advertRepository) {
        return new DeleteAdvertsAndStatisticsJob(
                statisticRepository,
                advertRepository,
                DeleteAdvertsAndStatisticsJob.class.getSimpleName(),
                SYSTEM,
                SYSTEM);
    }
}
