package ru.akvine.wild.bot.bot.converter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Message;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.integration.max.dto.Attachment;
import ru.akvine.wild.bot.services.integration.max.dto.Body;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.max.dto.PhotoAttachmentPayload;
import ru.akvine.wild.bot.services.integration.max.dto.ReceivedAttachment;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;

@Component
public class MaxDtoConverter implements BotDtoConverter<Update, SendMessageRequest> {
    private static final String CALLBACK_PAYLOAD_TYPE = "message_callback";
    private static final String IMAGE_ATTACHMENT_TYPE = "image";

    @Override
    public Payload fromRequest(Update update) {
        Payload payload = new Payload()
                .setChatId(getChatId(update))
                .setBotType(getType())
                .setFirstName(update.getUpdateMessage().getSender().getFirstName())
                .setLastName(update.getUpdateMessage().getSender().getLastName());

        if (CALLBACK_PAYLOAD_TYPE.equalsIgnoreCase(update.getUpdateType())) {
            payload.setBotDataType(BotDataType.CALLBACK);
            if (update.getCallback() != null && update.getCallback().getPayload() != null) {
                payload.setMessage(new Message().setText(update.getCallback().getPayload()));
            }
        } else {
            payload.setBotDataType(BotDataType.MESSAGE);
            if (update.getMessage() != null && update.getMessage().getBody() != null) {
                Body body = update.getMessage().getBody();
                Message message = new Message().setText(body.getText());
                findFirstImageUrl(body.getAttachments()).ifPresent(message::setMaxPhotoUrl);
                payload.setMessage(message);
            }
        }

        return payload;
    }

    /**
     * Ищет первое вложение типа {@code image} среди вложений сообщения и возвращает прямую
     * ссылку на файл.
     *
     * @param attachments вложения сообщения, может быть {@code null}/пустым
     * @return ссылка на первое фото-вложение, если оно есть
     */
    private Optional<String> findFirstImageUrl(List<ReceivedAttachment> attachments) {
        if (CollectionUtils.isEmpty(attachments)) {
            return Optional.empty();
        }

        return attachments.stream()
                .filter(attachment -> IMAGE_ATTACHMENT_TYPE.equalsIgnoreCase(attachment.getType()))
                .map(ReceivedAttachment::getPayload)
                .filter(Objects::nonNull)
                .map(PhotoAttachmentPayload::getUrl)
                .filter(StringUtils::isNotBlank)
                .findFirst();
    }

    @Override
    public SendMessageRequest toResponse(Response response) {
        MaxSendMessage maxSendMessage = response.getMaxSendMessage();
        SendMessageRequest request = new SendMessageRequest();
        if (maxSendMessage == null) {
            request.setText(response.getText());
        } else {
            request.setText(maxSendMessage.getText());
            if (CollectionUtils.isNotEmpty(maxSendMessage.getAttachments())) {
                request.setAttachments(maxSendMessage.getAttachments().toArray(new Attachment[0]));
            }
        }

        return request;
    }

    @Override
    public BotType getType() {
        return BotType.MAX;
    }

    private String getChatId(Update update) {
        return update.getUpdateMessage().getRecipient().getChatId();
    }
}
