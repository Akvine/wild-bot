package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Body {
    @JsonProperty(value = "mid")
    private String mid;

    @JsonProperty(value = "text")
    private String text;

    @JsonProperty(value = "attachments")
    private List<ReceivedAttachment> attachments;
}
