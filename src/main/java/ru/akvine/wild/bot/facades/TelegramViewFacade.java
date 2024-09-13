package ru.akvine.wild.bot.facades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.controllers.views.TelegramView;

import java.util.Map;

@Getter
@AllArgsConstructor
public class TelegramViewFacade {
    private final Map<ClientState, TelegramView> eventMap;
}
