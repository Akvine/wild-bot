package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AdvertListResponse {
    private List<AdvertStatisticDto> adverts;
    private int all;
}
