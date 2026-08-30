package ru.akvine.wild.bot.services.integration.max.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetUploadFileUrlResponse {
    @JsonProperty(value = "url")
    private String url;
}
