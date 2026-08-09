package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GoodDto {
    private String nmId;
    private int discount;
    private List<GoodSizeDto> sizes;
}
