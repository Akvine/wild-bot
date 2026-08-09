package ru.akvine.wild.bot.admin.dto.advert;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateAdvertRequest {
    private int advertId;

    private LocalDateTime availableForStart;
}
