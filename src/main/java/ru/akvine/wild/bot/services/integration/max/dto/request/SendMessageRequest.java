package ru.akvine.wild.bot.services.integration.max.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.services.integration.max.dto.Attachment;

@Data
@Accessors(chain = true)
public class SendMessageRequest {
    private String text;
    private Attachment[] attachments;
}
