package ru.akvine.wild.bot.services.dto.admin.advert;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class UpdateAdvert {
    private int advertId;

    @Nullable
    private LocalDateTime availableForStart;
}
