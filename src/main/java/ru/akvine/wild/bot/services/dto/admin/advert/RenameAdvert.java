package ru.akvine.wild.bot.services.dto.admin.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class RenameAdvert {
    @Nullable
    private String advertUuid;
    private Integer advertId;
    private String name;
}
