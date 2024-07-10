package ru.akvine.marketspace.bot.resolvers.state;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.TelegramData;

public interface StateResolver {
    BotApiMethod<?> resolve(TelegramData update);

    ClientState getState();
}
