package ru.akvine.wild.bot.max.bot;

import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;

@RequiredArgsConstructor
public class MaxDummyBot implements MaxBot {
    private final MaxIntegrationService maxIntegrationService;
    private final InitMessageFilter startMessageFilter;
    private final BotDtoConverterFacade facade;

    @Override
    public SendMessageRequest onUpdateReceived(Update[] updates) {
        if (updates.length != 0) {
            Update update = updates[0];
            Message[] messages = maxIntegrationService.getMessages(
                    update.getUpdateMessage().getRecipient().getChatId());

            if (messages.length != 0) {
                update.setMessage(messages[0]);

                Payload payload = facade.getConverter(BotType.MAX).fromRequest(update);
                Response response = startMessageFilter.handle(payload);

                return (SendMessageRequest) facade.getConverter(BotType.MAX).toResponse(response);
            }
        }

        return null;
    }
}
