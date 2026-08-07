package ru.akvine.wild.bot.services.integration.max.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetMessagesRequest {
    private String chatId;
}
