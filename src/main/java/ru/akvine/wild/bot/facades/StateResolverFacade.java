package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.controllers.states.StateResolver;
import ru.akvine.wild.bot.enums.ClientState;

@Getter
@AllArgsConstructor
public class StateResolverFacade {
    private final Map<ClientState, StateResolver> stateResolvers;
}
