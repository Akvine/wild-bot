package ru.akvine.wild.bot.services.integration.property.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PropertyResponse {
    private int count;

    private List<PropertyDto> properties;
}
