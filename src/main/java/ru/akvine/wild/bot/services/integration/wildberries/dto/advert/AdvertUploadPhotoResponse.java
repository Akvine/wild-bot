package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertUploadPhotoResponse {
    @JsonProperty(namespace = "error")
    private boolean hasError;
    private String errorText;
}
