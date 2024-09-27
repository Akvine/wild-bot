package ru.akvine.wild.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UpdateAdvertRequest {
    private int advertId;

    private LocalDateTime availableForStart;
}
