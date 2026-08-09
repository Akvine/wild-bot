package ru.akvine.wild.bot.services.integration.wildberries.dto.advert;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertParams {
    private AdvertSubject subject;
    private int cpm;
    private List<Integer> nms;
}
