package ru.akvine.wild.bot.services.integration;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.RetryException;
import ru.akvine.wild.bot.helpers.RetryHelper;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@Component
@RequiredArgsConstructor
public class BotIntegrationAdapterImpl implements BotIntegrationAdapter {
    private final TelegramIntegrationService telegramIntegrationService;
    private final MaxIntegrationService maxIntegrationService;

    private final RetryHelper retryHelper;

    @Value("${send.file.retry.attempts.count}")
    private int retryAttemptsCount;

    @Value("${send.file.retry.initial.delay.millis}")
    private int retryInitialDelayMillis;

    @Value("${send.file.retry.exponential.backoff.multiplier}")
    private double retryExponentialBackoffMultiplier;

    @Value("${send.file.retry.max.delay.millis}")
    private int retryMaxDelayMillis;

    @Override
    public void sendImage(String chatId, BotType botType, byte[] image, String caption) {
        if (BotType.TELEGRAM == botType) {
            telegramIntegrationService.sendImage(chatId, image, caption);
        } else {
            String url = maxIntegrationService.getUploadFileUrl(AttachmentType.IMAGE);
            String token = maxIntegrationService.uploadImageAtServer(url, image, caption);

            String errorMessage =
                    String.format("Retry attempts limit = [%s] exceeded for sending image message", retryAttemptsCount);
            retryHelper.retryWithExponentialBackoffWithoutResult(
                    retryAttemptsCount,
                    Duration.ofMillis(retryInitialDelayMillis),
                    retryExponentialBackoffMultiplier,
                    Duration.ofMillis(retryMaxDelayMillis),
                    () -> {
                        SendMessageRequest request = new SendMessageRequest()
                                .setAttachments(MaxComponentsFactory.createFileAttachment(AttachmentType.IMAGE, token));
                        maxIntegrationService.sendMessage(chatId, request);
                    },
                    new RetryException(errorMessage));
        }
    }

    @Override
    public void sendFile(String chatId, BotType botType, byte[] file, String fileName) {
        if (BotType.TELEGRAM == botType) {
            telegramIntegrationService.sendFile(chatId, fileName, file);
        } else {
            String url = maxIntegrationService.getUploadFileUrl(AttachmentType.FILE);
            String token = maxIntegrationService.uploadFileAtServer(url, file, fileName);

            String errorMessage =
                    String.format("Retry attempts limit = [%s] exceeded for sending file message", retryAttemptsCount);
            retryHelper.retryWithExponentialBackoffWithoutResult(
                    retryAttemptsCount,
                    Duration.ofMillis(retryInitialDelayMillis),
                    retryExponentialBackoffMultiplier,
                    Duration.ofMillis(retryMaxDelayMillis),
                    () -> {
                        SendMessageRequest request = new SendMessageRequest()
                                .setAttachments(MaxComponentsFactory.createFileAttachment(AttachmentType.FILE, token));
                        maxIntegrationService.sendMessage(chatId, request);
                    },
                    new RetryException(errorMessage));
        }
    }

    @Override
    public void sendMessage(Set<String> chatIds, BotType botType, String message) {
        if (BotType.TELEGRAM == botType) {
            telegramIntegrationService.sendMessage(chatIds, message);
        } else {
            SendMessageRequest request = new SendMessageRequest().setText(message);
            for (String chatId : chatIds) {
                maxIntegrationService.sendMessage(chatId, request);
            }
        }
    }
}
