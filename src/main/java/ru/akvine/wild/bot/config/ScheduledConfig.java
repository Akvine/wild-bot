package ru.akvine.wild.bot.config;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxHandler;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxProperties;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxRelay;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStore;
import ru.akvine.wild.bot.infrastructure.property.printers.PropertiesPrinter;
import ru.akvine.wild.bot.job.CheckRunningAdvertsJob;
import ru.akvine.wild.bot.job.PrintPropertiesJob;
import ru.akvine.wild.bot.job.SubscriptionJob;
import ru.akvine.wild.bot.job.domain.DeleteAdvertsAndStatisticsJob;
import ru.akvine.wild.bot.job.sync.*;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.integration.custodian.CustodianIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.outbox.BotMessageOutbox;
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
            BotMessageOutbox botMessageOutbox,
            PropertyService propertyService,
            TransactionTemplate transactionTemplate) {
        return new CheckRunningAdvertsJob(
                advertRepository,
                botMessageOutbox,
                wildberriesIntegrationService,
                countersStorage,
                advertStatisticService,
                propertyService,
                transactionTemplate,
                CheckRunningAdvertsJob.class.getSimpleName(),
                SYSTEM,
                SYSTEM);
    }

    @Bean
    public SubscriptionJob subscriptionJob(
            BotMessageOutbox botMessageOutbox, SubscriptionRepository subscriptionRepository) {
        return new SubscriptionJob(
                botMessageOutbox, subscriptionRepository, SubscriptionJob.class.getSimpleName(), SYSTEM);
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
    public DeleteAdvertsAndStatisticsJob deleteAdvertsAndStatisticsJob(
            AdvertStatisticRepository statisticRepository, AdvertRepository advertRepository) {
        return new DeleteAdvertsAndStatisticsJob(
                statisticRepository,
                advertRepository,
                DeleteAdvertsAndStatisticsJob.class.getSimpleName(),
                SYSTEM,
                SYSTEM);
    }

    /**
     * Relay transactional outbox: доставляет записанные в БД сообщения клиентам (см. {@link OutboxRelay}).
     * Выключается свойством {@code outbox.relay.enabled=false} (например, в тестах).
     */
    @Bean
    @ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true")
    public OutboxRelay outboxRelay(
            OutboxStore outboxStore, List<OutboxHandler> outboxHandlers, OutboxProperties outboxProperties) {
        return new OutboxRelay(outboxStore, outboxHandlers, outboxProperties);
    }
}
