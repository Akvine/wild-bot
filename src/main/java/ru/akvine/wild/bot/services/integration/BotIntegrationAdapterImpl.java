package ru.akvine.wild.bot.services.integration;

import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.retry.RetryExecutor;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@Component
@RequiredArgsConstructor
public class BotIntegrationAdapterImpl implements BotIntegrationAdapter {
    private static final int TOO_MANY_REQUESTS = 429;

    /**
     * Ошибки 4xx (кроме 429) повторять бессмысленно: запрос некорректен и с тем же телом получит тот же ответ
     */
    private static final Predicate<Exception> RETRY_ON_TRANSIENT_ERRORS = exception ->
            !(exception instanceof HttpClientErrorException clientError)
                    || clientError.getStatusCode().value() == TOO_MANY_REQUESTS;

    private final TelegramIntegrationService telegramIntegrationService;
    private final MaxIntegrationService maxIntegrationService;

    private final RetryExecutor retryExecutor;

    @Override
    public void sendImage(String chatId, BotType botType, byte[] image, String caption) {
        if (BotType.TELEGRAM == botType) {
            telegramIntegrationService.sendImage(chatId, image, caption);
        } else {
            String url = maxIntegrationService.getUploadFileUrl(AttachmentType.IMAGE);
            String token = maxIntegrationService.uploadImageAtServer(url, image, caption);

            retryExecutor.execute(
                    () -> {
                        SendMessageRequest request = new SendMessageRequest()
                                .setAttachments(MaxComponentsFactory.createFileAttachment(AttachmentType.IMAGE, token));
                        maxIntegrationService.sendMessage(chatId, request);
                    },
                    RETRY_ON_TRANSIENT_ERRORS,
                    "Sending image message");
        }
    }

    @Override
    public void sendFile(String chatId, BotType botType, byte[] file, String fileName) {
        if (BotType.TELEGRAM == botType) {
            telegramIntegrationService.sendFile(chatId, fileName, file);
        } else {
            String url = maxIntegrationService.getUploadFileUrl(AttachmentType.FILE);
            String token = maxIntegrationService.uploadFileAtServer(url, file, fileName);

            retryExecutor.execute(
                    () -> {
                        SendMessageRequest request = new SendMessageRequest()
                                .setAttachments(MaxComponentsFactory.createFileAttachment(AttachmentType.FILE, token));
                        maxIntegrationService.sendMessage(chatId, request);
                    },
                    RETRY_ON_TRANSIENT_ERRORS,
                    "Sending file message");
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
