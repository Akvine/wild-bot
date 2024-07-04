package ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertBudgetDepositRequest {
    private int sum;
    private int type;
    @JsonProperty(value = "return")
    private boolean isReturn;
}
