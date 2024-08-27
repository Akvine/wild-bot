package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertFullStatisticIntervalDto {
    private int id;
    private AdvertStatisticInterval interval;
}
