package ru.akvine.wild.bot.services.integration.property.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PropertyDto {
    private String profile;
    private String description;
    private String key;
    private String value;
}
