package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertDto {
    private int advertId;
    private String name;
    private Date changeTime;
    private int type;
    private int status;

    @JsonProperty(value = "autoParams")
    private AdvertParams advertParams;
}
