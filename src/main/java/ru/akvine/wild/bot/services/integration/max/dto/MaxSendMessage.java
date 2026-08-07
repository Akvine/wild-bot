package ru.akvine.wild.bot.services.integration.max.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MaxSendMessage {
    private String chatId;
    private String text;
    private List<Attachment> attachments;
}
