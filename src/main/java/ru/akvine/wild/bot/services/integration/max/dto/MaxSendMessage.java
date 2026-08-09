package ru.akvine.wild.bot.services.integration.max.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MaxSendMessage {
    private String chatId;
    private String text;
    private List<Attachment> attachments;
}
