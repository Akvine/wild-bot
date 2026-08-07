package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Body {
    @JsonProperty(value = "mid")
    private String mid;

    @JsonProperty(value = "mid")
    private String text;
}
