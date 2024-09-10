package ru.akvine.wild.bot.services.integration.property.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetPropertiesRequest {
    private String profile;
}
