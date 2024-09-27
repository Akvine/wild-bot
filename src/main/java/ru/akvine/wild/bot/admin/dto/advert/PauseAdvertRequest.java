package ru.akvine.wild.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PauseAdvertRequest {
    private String advertUuid;

    private Integer advertId;
}
