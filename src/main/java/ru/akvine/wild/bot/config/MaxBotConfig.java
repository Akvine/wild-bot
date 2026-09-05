package ru.akvine.wild.bot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;
import ru.akvine.wild.bot.max.bot.MaxBot;
import ru.akvine.wild.bot.max.bot.MaxDevBot;
import ru.akvine.wild.bot.max.bot.MaxDummyBot;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;

@Configuration
public class MaxBotConfig {

    @Bean
    @ConditionalOnProperty(name = "max.bot.type", havingValue = "longpooling")
    public MaxBot maxLongPoolingBot(
            MaxIntegrationService maxIntegrationService,
            InitMessageFilter startMessageFilter,
            BotDtoConverterFacade facade) {
        return new MaxDevBot(maxIntegrationService, startMessageFilter, facade);
    }

    @Bean
    @ConditionalOnProperty(name = "max.bot.type", havingValue = "dummy")
    public MaxBot maxDummyBot(
            MaxIntegrationService maxIntegrationService,
            InitMessageFilter startMessageFilter,
            BotDtoConverterFacade facade) {
        return new MaxDummyBot(maxIntegrationService, startMessageFilter, facade);
    }
}
