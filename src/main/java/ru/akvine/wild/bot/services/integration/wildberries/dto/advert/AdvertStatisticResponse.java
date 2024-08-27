package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertStatisticResponse {
    private String views;
    private String clicks;
    private String ctr;
    private String cpc;
    private String spend;
}
