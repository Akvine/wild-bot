package ru.akvine.marketspace.bot.resolvers.controllers.state;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.TelegramData;

public interface StateResolver {
    BotApiMethod<?> resolve(TelegramData telegramData);

    ClientState getState();

    default SendMessage setNextState(String chatId, ClientState nextState) {
        return null;
    }
}
