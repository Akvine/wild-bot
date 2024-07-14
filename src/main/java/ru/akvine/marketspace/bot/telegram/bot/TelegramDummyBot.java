package ru.akvine.marketspace.bot.telegram.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class TelegramDummyBot extends TelegramWebhookBot {
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        logger.info("Send message in dummy bot = {}", update);
        return null;
    }

    @Override
    public String getBotPath() {
        return "";
    }

    @Override
    public String getBotUsername() {
        return "";
    }
}
