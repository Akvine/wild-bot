package ru.akvine.marketspace.bot.services.dto.admin.advert;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PauseAdvert {
    private String advertUuid;
    private String advertId;
}
