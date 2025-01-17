package ru.akvine.wild.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.controllers.states.StateResolver;
import ru.akvine.wild.bot.controllers.views.TelegramView;
import ru.akvine.wild.bot.enums.*;
import ru.akvine.wild.bot.facades.*;
import ru.akvine.wild.bot.facades.proxy.WildberriesProxiesFacade;
import ru.akvine.wild.bot.infrastructure.property.maskers.PropertyMasker;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.resolvers.property.PropertyParser;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationService;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationServiceType;
import ru.akvine.wild.bot.services.integration.wildberries.proxy.WildberriesIntegrationServiceProxy;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Configuration
public class FacadesConfig {

    @Bean
    public TelegramDataResolverFacade telegramDataResolverFacade(List<TelegramDataResolver> resolvers) {
        Map<TelegramDataType, TelegramDataResolver> updateDataExtractorMap = resolvers
                .stream()
                .collect(toMap(TelegramDataResolver::getType, identity()));
        return new TelegramDataResolverFacade(updateDataExtractorMap);
    }

    @Bean
    public StateResolverFacade stateResolverFacade(List<StateResolver> stateResolvers) {
        Map<ClientState, StateResolver> stateResolversMap = stateResolvers
                .stream()
                .collect(toMap(StateResolver::getState, identity()));
        return new StateResolverFacade(stateResolversMap);
    }

    @Bean
    public CommandResolverFacade commandResolverFacade(List<CommandResolver> commandResolvers) {
        Map<Command, CommandResolver> commandResolverMap = commandResolvers
                .stream()
                .collect(toMap(CommandResolver::getCommand, identity()));
        return new CommandResolverFacade(commandResolverMap);
    }

    @Bean
    public TelegramViewFacade telegramEventFacade(List<TelegramView> telegramViews) {
        Map<ClientState, TelegramView> keyboardMap = telegramViews
                .stream()
                .collect(toMap(TelegramView::byState, identity()));
        return new TelegramViewFacade(keyboardMap);
    }

    @Bean
    public QrCodeGenerationServiceFacade qrCodeGenerationServiceFacade(List<QrCodeGenerationService> qrCodeGenerationServices) {
        Map<QrCodeGenerationServiceType, QrCodeGenerationService> serviceMap = qrCodeGenerationServices
                .stream()
                .collect(toMap(QrCodeGenerationService::getType, identity()));
        return new QrCodeGenerationServiceFacade(serviceMap);
    }

    @Bean
    public PropertyParseFacade propertyParseFacade(List<PropertyParser<?>> propertyParsers) {
        Map<Class<?>, PropertyParser<?>> propertiesMap = propertyParsers
                .stream()
                .collect(toMap(PropertyParser::getType, identity()));
        return new PropertyParseFacade(propertiesMap);
    }

    @Bean
    public SensitivePropertyMaskersFacade sensitivePropertyMaskersFacade(List<PropertyMasker> maskers) {
        Map<SensitiveDataType, PropertyMasker> map = maskers
                .stream()
                .collect(toMap(PropertyMasker::getType, identity()));
        return new SensitivePropertyMaskersFacade(map);
    }

    @Bean
    public WildberriesProxiesFacade wildberriesProxiesFacade(List<WildberriesIntegrationServiceProxy> proxies) {
        Map<ProxyType, WildberriesIntegrationServiceProxy> proxiesMap = proxies
                .stream()
                .collect(toMap(WildberriesIntegrationServiceProxy::getType, identity()));
        return new WildberriesProxiesFacade(proxiesMap);
    }
}
