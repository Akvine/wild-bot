package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertCreateRequest {
    private int type;
    private String name;
    private int subjectId;
    private int sum;
    @JsonProperty(value = "on_pause")
    private boolean onPause;
    private int btype;
    private int[] nms;
    private int cpm;
}
