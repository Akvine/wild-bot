package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GoodDto {
    private String nmId;
    private int discount;
    private List<GoodSizeDto> sizes;
}
