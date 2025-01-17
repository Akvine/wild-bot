package ru.akvine.wild.bot.telegram.bot;


import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.telegram.filter.MessageFilter;

public class TelegramDevBot extends TelegramLongPollingBot {
    private final String botUsername;

    private final MessageFilter startMessageFilter;

    public TelegramDevBot(DefaultBotOptions options, String botToken, String botUsername, MessageFilter startMessageFilter) {
        super(options, botToken);
        this.botUsername = botUsername;
        this.startMessageFilter = startMessageFilter;
    }

    @Override
    public void onUpdateReceived(Update update) {
        sendMessage(startMessageFilter.handle(update));
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (Exception exception) {
            throw new RuntimeException("Some error has occurred, ex=" + exception.getMessage());
        }
    }
}
