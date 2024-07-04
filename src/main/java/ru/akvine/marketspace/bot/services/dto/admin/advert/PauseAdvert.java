package ru.akvine.marketspace.bot.services.dto.admin.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class PauseAdvert {
    @Nullable
    private String advertId;
    @Nullable
    private String advertUuid;
}
