package ru.akvine.wild.bot.bot.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Message;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.integration.max.dto.Attachment;
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
                .setFirstName(update.getUser().getFirstName())
                .setLastName(update.getUser().getLastName())
                .setLastName(update.getUser().getUsername());

        if (CALLBACK_PAYLOAD_TYPE.equalsIgnoreCase(update.getUpdateType())) {
            payload.setBotDataType(BotDataType.CALLBACK);
        } else {
            payload.setBotDataType(BotDataType.MESSAGE);
        }

        if (update.getMessage() != null && update.getMessage().getBody() != null) {
            payload.setMessage(new Message()
                    .setText(update.getMessage().getBody().getText()));
        }

        return payload;
    }

    @Override
    public SendMessageRequest toResponse(Response response) {
        SendMessageRequest request = new SendMessageRequest().setText(response.getMaxText());

        if (CollectionUtils.isEmpty(response.getAttachments())) {
            int attachmentSize = response.getAttachments().size();
            Attachment[] attachments = new Attachment[attachmentSize];
            for (int i = 0; i < attachmentSize; ++i) {
                attachments[i] = new Attachment()
                        .setType(response.getAttachments().get(i).getType())
                        .setPayload(response.getAttachments().get(i).getPayload());
            }

            request.setAttachments(attachments);
        }

        return request;
    }

    @Override
    public BotType getType() {
        return BotType.MAX;
    }

    private String getChatId(Update update) {
        return update.getChatId();
    }
}
