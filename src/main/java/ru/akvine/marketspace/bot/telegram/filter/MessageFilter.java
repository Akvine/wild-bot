package ru.akvine.marketspace.bot.telegram.filter;

import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Setter
public abstract class MessageFilter {
    protected MessageFilter messageFilter;

    public abstract BotApiMethod<?> handle(Update update);

    public String getChatId(Update update) {
        if (update.getCallbackQuery() != null) {
            return String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        } else {
            return String.valueOf(update.getMessage().getChatId());
        }
    }
}
