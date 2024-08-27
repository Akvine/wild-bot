package ru.akvine.marketspace.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.enums.TelegramDataType;
import ru.akvine.marketspace.bot.managers.CommandResolverManager;
import ru.akvine.marketspace.bot.managers.StateResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.command.CommandResolver;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.resolvers.controllers.views.TelegramView;
import ru.akvine.marketspace.bot.resolvers.controllers.states.StateResolver;

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
}
