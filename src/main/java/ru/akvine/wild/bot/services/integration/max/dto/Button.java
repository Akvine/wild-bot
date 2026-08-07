package ru.akvine.wild.bot.services.integration.max.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Button {
    private String type;
    private String text;
    private String url;
}
