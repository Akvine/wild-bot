package ru.akvine.wild.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.resolvers.controllers.views.TelegramView;

import java.util.Map;

@Getter
@AllArgsConstructor
public class TelegramViewManager {
    private final Map<ClientState, TelegramView> eventMap;
}
