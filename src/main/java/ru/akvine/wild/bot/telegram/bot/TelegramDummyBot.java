package ru.akvine.wild.bot.telegram.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;

@Slf4j
@RequiredArgsConstructor
public class TelegramDummyBot extends TelegramWebhookBot {
    private final InitMessageFilter messageFilter;
    private final BotDtoConverterFacade facade;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        logger.info("Send message in dummy bot = {}", update);
        Payload payload = facade.getConverter(BotType.TELEGRAM).fromRequest(update);
        Response response = messageFilter.handle(payload);
        return (BotApiMethod<?>) facade.getConverter(BotType.TELEGRAM).toResponse(response);
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
