package ru.akvine.wild.bot.services.integration.wildberries.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SettingsDto {
    private CursorDto cursor;
    private FilterDto filter;
}
