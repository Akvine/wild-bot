package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Update {
    @JsonProperty(value = "update_type")
    private String updateType;

    @JsonProperty(value = "timestamp")
    private int timestamp;

    @JsonProperty(value = "chat_id")
    private String chatId;

    @JsonProperty(value = "user")
    private User user;

    @JsonProperty(value = "is_channel")
    private boolean channel;
}
