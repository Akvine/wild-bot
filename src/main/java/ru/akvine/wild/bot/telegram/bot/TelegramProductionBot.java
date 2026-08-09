package ru.akvine.wild.bot.telegram.bot;

import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;

@Setter
public class TelegramProductionBot extends TelegramWebhookBot {
    private String botPath;
    private String botUsername;
    private final MessageFilter messageFilter;
    private final BotDtoConverterFacade facade;

    public TelegramProductionBot(
            DefaultBotOptions defaultBotOptions,
            String botToken,
            MessageFilter messageFilter,
            BotDtoConverterFacade facade) {
        super(defaultBotOptions, botToken);
        this.messageFilter = messageFilter;
        this.facade = facade;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        Payload payload = facade.getConverter(BotType.TELEGRAM).fromRequest(update);
        Response response = messageFilter.handle(payload);
        return (BotApiMethod<?>) facade.getConverter(BotType.TELEGRAM).toResponse(response);
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
