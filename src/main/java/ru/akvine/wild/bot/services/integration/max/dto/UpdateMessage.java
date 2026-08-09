package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateMessage {
    @JsonProperty(value = "sender")
    private Sender sender;

    @JsonProperty(value = "body")
    private Body body;

    @JsonProperty(value = "recipient")
    private Recipient recipient;

    @JsonProperty(value = "timestamp")
    private long timestamp;
}
