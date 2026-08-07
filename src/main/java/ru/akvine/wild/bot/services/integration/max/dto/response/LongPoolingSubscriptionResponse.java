package ru.akvine.wild.bot.services.integration.max.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.services.integration.max.dto.Update;

@Data
@Accessors(chain = true)
public class LongPoolingSubscriptionResponse {
    @JsonProperty(value = "updates")
    private Update[] updates;

    @JsonProperty(value = "marker")
    private int marker;
}
