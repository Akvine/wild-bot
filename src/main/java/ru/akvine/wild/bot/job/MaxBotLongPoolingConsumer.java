package ru.akvine.wild.bot.job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;

@RequiredArgsConstructor
public class MaxBotLongPoolingConsumer {
    private final MaxIntegrationService maxIntegrationService;
    private final MessageFilter startMessageFilter;
    private final BotDtoConverterFacade facade;

    @Scheduled(fixedDelayString = "${max.bot.dev.mode.long.pooling.cron.milliseconds}")
    public void checkUpdates() {
        Update[] updates = maxIntegrationService.updates();
        if (updates.length != 0) {
            Update update = updates[0];
            Message[] messages = maxIntegrationService.getMessages(
                    update.getUpdateMessage().getRecipient().getChatId());

            if (messages.length != 0) {
                update.setMessage(messages[0]);

                Payload payload = facade.getConverter(BotType.MAX).fromRequest(update);
                Response response = startMessageFilter.handle(payload);

                SendMessageRequest sendMessageRequest =
                        (SendMessageRequest) facade.getConverter(BotType.MAX).toResponse(response);
                maxIntegrationService.sendMessage(payload.getChatId(), sendMessageRequest);
            }
        }
    }
}
