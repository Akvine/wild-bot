package ru.akvine.marketspace.bot.telegram;


import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;

public class TelegramLongPoolingBot extends TelegramLongPollingBot {
    private final String botUsername;

    private final MessageDispatcher messageDispatcher;

    public TelegramLongPoolingBot(DefaultBotOptions options, String botToken, String botUsername, MessageDispatcher messageDispatcher) {
        super(options, botToken);
        this.botUsername = botUsername;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void onUpdateReceived(Update update) {
        sendMessage(messageDispatcher.doDispatch(update));
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
