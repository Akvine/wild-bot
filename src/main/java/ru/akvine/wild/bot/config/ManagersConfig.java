package ru.akvine.wild.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.enums.TelegramDataType;
import ru.akvine.wild.bot.managers.*;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.resolvers.controllers.views.TelegramView;
import ru.akvine.wild.bot.resolvers.controllers.states.StateResolver;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationService;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationServiceType;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class ManagersConfig {

    @Bean
    public TelegramDataResolverManager telegramDataResolverManager(List<TelegramDataResolver> resolvers) {
        Map<TelegramDataType, TelegramDataResolver> updateDataExtractorMap = resolvers
                .stream()
                .collect(toMap(TelegramDataResolver::getType, identity()));
        return new TelegramDataResolverManager(updateDataExtractorMap);
    }

    @Bean
    public StateResolverManager stateResolverManager(List<StateResolver> stateResolvers) {
        Map<ClientState, StateResolver> stateResolversMap = stateResolvers
                .stream()
                .collect(toMap(StateResolver::getState, identity()));
        return new StateResolverManager(stateResolversMap);
    }

    @Bean
    public CommandResolverManager commandResolverManager(List<CommandResolver> commandResolvers) {
        Map<Command, CommandResolver> commandResolverMap = commandResolvers
                .stream()
                .collect(toMap(CommandResolver::getCommand, identity()));
        return new CommandResolverManager(commandResolverMap);
    }

    @Bean
    public TelegramViewManager telegramEventManager(List<TelegramView> telegramViews) {
        Map<ClientState, TelegramView> keyboardMap = telegramViews
                .stream()
                .collect(toMap(TelegramView::byState, identity()));
        return new TelegramViewManager(keyboardMap);
    }

    @Bean
    public QrCodeGenerationServiceManager qrCodeGenerationServiceManager(List<QrCodeGenerationService> qrCodeGenerationServices) {
        Map<QrCodeGenerationServiceType, QrCodeGenerationService> serviceMap = qrCodeGenerationServices
                .stream()
                .collect(toMap(QrCodeGenerationService::getType, identity()));
        return new QrCodeGenerationServiceManager(serviceMap);
    }
}
