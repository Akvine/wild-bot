package ru.akvine.wild.bot.facades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.controllers.views.BotView;

import java.util.Map;

@Getter
@AllArgsConstructor
public class BotViewFacade {
    private final Map<ClientState, BotView> eventMap;
}
