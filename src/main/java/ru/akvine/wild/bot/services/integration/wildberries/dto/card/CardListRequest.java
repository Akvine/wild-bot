package ru.akvine.wild.bot.services.integration.wildberries.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardListRequest {
    private SettingsDto settings;
}
