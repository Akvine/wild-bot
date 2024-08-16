package ru.akvine.marketspace.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.resolvers.controllers.view.TelegramView;

import java.util.Map;

@Getter
@AllArgsConstructor
public class TelegramViewManager {
    private final Map<ClientState, TelegramView> eventMap;
}
