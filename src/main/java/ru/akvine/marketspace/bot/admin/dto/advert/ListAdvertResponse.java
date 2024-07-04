package ru.akvine.marketspace.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SuccessfulResponse;

import java.util.List;

@Data
@Accessors(chain = true)
public class ListAdvertResponse extends SuccessfulResponse {
    private int count;
    private List<AdvertDto> adverts;
}
