package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Recipient {
    @JsonProperty(value = "chat_id")
    private String chatId;

    @JsonProperty(value = "chat_type")
    private String chatType;

    @JsonProperty(value = "user_id")
    private long userId;
}
