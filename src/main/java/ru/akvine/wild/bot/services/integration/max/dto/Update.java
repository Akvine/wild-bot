package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Update {
    @JsonProperty(value = "update_type")
    private String updateType;

    @JsonProperty(value = "timestamp")
    private long timestamp;

    @JsonProperty(value = "message")
    private UpdateMessage updateMessage;

    @JsonProperty(value = "callback")
    private Callback callback;

    @JsonIgnore
    private Message message;
}
