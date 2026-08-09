package ru.akvine.wild.bot.bot.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Message;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.integration.max.dto.Attachment;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;

@Component
public class MaxDtoConverter implements BotDtoConverter<Update, SendMessageRequest> {
    private static final String CALLBACK_PAYLOAD_TYPE = "message_callback";

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
                payload.setMessage(new Message()
                        .setText(update.getMessage().getBody().getText()));
            }
        }

        return payload;
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
