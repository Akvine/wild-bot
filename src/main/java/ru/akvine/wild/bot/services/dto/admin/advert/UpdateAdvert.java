package ru.akvine.wild.bot.services.dto.admin.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UpdateAdvert {
    private int advertId;
    @Nullable
    private LocalDateTime availableForStart;
}
