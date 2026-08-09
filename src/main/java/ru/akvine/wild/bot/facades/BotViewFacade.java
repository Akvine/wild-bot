package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.ClientState;

@Getter
@AllArgsConstructor
public class BotViewFacade {
    private final Map<ClientState, BotView> eventMap;
}
