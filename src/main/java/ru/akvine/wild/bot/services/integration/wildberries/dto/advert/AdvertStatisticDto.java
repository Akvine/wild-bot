package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertStatisticDto {
    private int count;
    private int status;
    private int type;

    @JsonProperty("advert_list")
    private List<AdvertDto> advertList;
}
