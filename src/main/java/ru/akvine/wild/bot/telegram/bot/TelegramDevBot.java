package ru.akvine.wild.bot.telegram.bot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;

public class TelegramDevBot extends TelegramLongPollingBot {
    private final String botUsername;

    private final MessageFilter startMessageFilter;
    private final BotDtoConverterFacade facade;

    public TelegramDevBot(
            DefaultBotOptions options,
            String botToken,
            String botUsername,
            MessageFilter startMessageFilter,
            BotDtoConverterFacade facade) {
        super(options, botToken);
        this.botUsername = botUsername;
        this.startMessageFilter = startMessageFilter;
        this.facade = facade;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Payload payload = facade.getConverter(BotType.TELEGRAM).fromRequest(update);
        Response response = startMessageFilter.handle(payload);
        sendMessage((BotApiMethod<?>) facade.getConverter(BotType.TELEGRAM).toResponse(response));
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
