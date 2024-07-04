package ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertFullStatisticResponse {
    private String advertId;
    private String views;
    private String clicks;
    private String ctr;
    private String cpc;
    private String sum;
    private String atbs;
    private String orders;
    private String cr;
    private String shks;
    @JsonProperty(value = "sum_price")
    private String sumPrice;
}
