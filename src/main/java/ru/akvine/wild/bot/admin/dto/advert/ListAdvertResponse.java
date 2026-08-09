package ru.akvine.wild.bot.admin.dto.advert;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

@Data
@Accessors(chain = true)
public class ListAdvertResponse extends SuccessfulResponse {
    private int count;
    private List<AdvertDto> adverts;
}
