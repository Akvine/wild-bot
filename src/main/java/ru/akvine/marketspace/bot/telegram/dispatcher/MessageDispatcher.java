package ru.akvine.marketspace.bot.telegram.dispatcher;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageDispatcher {
    BotApiMethod<?> doDispatch(Update update);
}
