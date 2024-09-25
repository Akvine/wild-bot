package ru.akvine.wild.bot.services.integration.trimly.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShortUrlResponse {
    private String shortUrl;
}
