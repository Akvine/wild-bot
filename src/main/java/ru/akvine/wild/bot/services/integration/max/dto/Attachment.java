package ru.akvine.wild.bot.services.integration.max.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Attachment {
    private String type;
    private Payload payload;
}
