package ru.akvine.wild.bot.services.dto.admin.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.AdvertStatus;

import java.util.List;

@Data
@Accessors(chain = true)
public class ListAdvert {
    private List<AdvertStatus> statuses;
}
