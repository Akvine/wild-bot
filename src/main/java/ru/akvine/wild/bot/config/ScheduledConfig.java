package ru.akvine.wild.bot.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.property.printers.PropertiesPrinter;
import ru.akvine.wild.bot.job.CheckRunningAdvertsJob;
import ru.akvine.wild.bot.job.MaxBotLongPoolingConsumer;
import ru.akvine.wild.bot.job.PrintPropertiesJob;
import ru.akvine.wild.bot.job.SubscriptionJob;
import ru.akvine.wild.bot.job.monitoring.HikariPoolMetricsJob;
import ru.akvine.wild.bot.job.sync.*;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.property.PropertyService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;

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
            BotIntegrationAdapter botIntegrationAdapter) {
        return new CheckRunningAdvertsJob(
                advertRepository,
                botIntegrationAdapter,
                wildberriesIntegrationService,
                countersStorage,
                advertStatisticService,
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
    @ConditionalOnProperty(name = "sync.application.properties.enabled", havingValue = "true")
    public SyncPropertiesJob syncPropertiesJob(PropertyService propertyService) {
        return new SyncPropertiesJob(propertyService);
    }

    @Bean
    @ConditionalOnProperty(name = "print.properties.enabled", havingValue = "true")
    public PrintPropertiesJob printPropertiesJob(PropertyService propertyService, PropertiesPrinter propertiesPrinter) {
        return new PrintPropertiesJob(propertyService, propertiesPrinter);
    }

    @Bean
    @ConditionalOnExpression("${max.bot.enabled:false} && ${max.bot.dev.mode.enabled:false}")
    public MaxBotLongPoolingConsumer maxBotLongPoolingJob(
            MaxIntegrationService maxIntegrationService,
            InitMessageFilter messageFilter,
            BotDtoConverterFacade converters) {
        return new MaxBotLongPoolingConsumer(maxIntegrationService, messageFilter, converters);
    }

    @Bean
    @ConditionalOnProperty(name = "hikari.pool.metrics.log.enabled", havingValue = "true")
    public HikariPoolMetricsJob hikariPoolMetricsJob(HikariDataSource hikariDataSource) {
        return new HikariPoolMetricsJob(hikariDataSource);
    }
}
