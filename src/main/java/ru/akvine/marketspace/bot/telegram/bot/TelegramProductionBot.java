package ru.akvine.marketspace.bot.telegram.bot;

import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.telegram.filter.MessageFilter;

@Setter
public class TelegramProductionBot extends TelegramWebhookBot {
    private String botPath;
    private String botUsername;
    private final MessageFilter messageFilter;

    public TelegramProductionBot(DefaultBotOptions defaultBotOptions,
                                 String botToken,
                                 MessageFilter messageFilter) {
        super(defaultBotOptions, botToken);
        this.messageFilter = messageFilter;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return messageFilter.handle(update);
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
