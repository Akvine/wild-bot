package ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AdvertParams {
    private AdvertSubject subject;
    private Integer cpm;
    private List<String> nms;
}
