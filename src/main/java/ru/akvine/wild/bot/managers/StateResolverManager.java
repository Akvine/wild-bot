package ru.akvine.marketspace.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.resolvers.controllers.states.StateResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class StateResolverManager {
    private final Map<ClientState, StateResolver> stateResolvers;
}
