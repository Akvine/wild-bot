package ru.akvine.marketspace.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertStatisticDto {
    private String views;
    private String clicks;
    private String ctr;
    private String cpc;
    private String sum;
    private String atbs;
    private String orders;
    private String cr;
    private String shks;
    private String sumPrice;
}
