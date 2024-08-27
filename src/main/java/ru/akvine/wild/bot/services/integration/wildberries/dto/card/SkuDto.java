package ru.akvine.marketspace.bot.services.integration.wildberries.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkuDto {
    private String sku;
    private int amount;
}
