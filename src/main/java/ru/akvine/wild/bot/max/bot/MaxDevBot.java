package ru.akvine.wild.bot.max.bot;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
public class MaxDevBot implements MaxBot {
    private static final Logger log = LoggerFactory.getLogger(MaxDevBot.class);
    private final MaxIntegrationService maxIntegrationService;
    private final InitMessageFilter startMessageFilter;
    private final BotDtoConverterFacade facade;

    @Value("${max.bot.long.pooling.cron.milliseconds}")
    private long pollingDelayMillis;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::pollAndProcess, 0, pollingDelayMillis, TimeUnit.MILLISECONDS);
    }

    private void pollAndProcess() {
        try {
            Update[] updates = maxIntegrationService.updates();
            if (updates.length > 0) {
                onUpdateReceived(updates);
            }
        } catch (Exception e) {
            log.error(
                    "Error while poll and process max bot messages = {}. Stacktrace = {}",
                    e.getMessage(),
                    e.getStackTrace());
        }
    }

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

                SendMessageRequest sendMessageRequest =
                        (SendMessageRequest) facade.getConverter(BotType.MAX).toResponse(response);
                maxIntegrationService.sendMessage(payload.getChatId(), sendMessageRequest);
                return sendMessageRequest;
            }
        }

        return SendMessageRequest.empty();
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
