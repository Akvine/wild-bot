package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LongPoolingSubscriptionResponse {
    @JsonProperty(value = "updates")
    private Update[] updates;

    @JsonProperty(value = "marker")
    private int marker;
}
