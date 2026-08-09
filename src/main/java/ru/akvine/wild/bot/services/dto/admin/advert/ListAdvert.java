package ru.akvine.wild.bot.services.dto.admin.advert;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.AdvertStatus;

@Data
@Accessors(chain = true)
public class ListAdvert {
    private List<AdvertStatus> statuses;
}
