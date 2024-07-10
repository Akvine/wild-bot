package ru.akvine.marketspace.bot.telegram.filter;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Setter
public abstract class MessageFilter {
    protected MessageFilter messageFilter;

    public abstract BotApiMethod<?> handle(Update update);
}
