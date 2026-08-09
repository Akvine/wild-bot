package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Callback {
    @JsonProperty(value = "timestamp")
    private long timestamp;

    @JsonProperty(value = "callback_id")
    private String callbackId;

    @JsonProperty(value = "user")
    private User user;

    @JsonProperty(value = "payload")
    private String payload;
}
