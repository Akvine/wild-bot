package ru.akvine.wild.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.resolvers.controllers.states.StateResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class StateResolverManager {
    private final Map<ClientState, StateResolver> stateResolvers;
}
