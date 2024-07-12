package ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetGoodsRequest {
    private int limit;
    private Integer filterNmID;
}
