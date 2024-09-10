package ru.akvine.wild.bot.services.integration.property.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PropertyResponse {
    private int count;

    private List<PropertyDto> properties;
}
