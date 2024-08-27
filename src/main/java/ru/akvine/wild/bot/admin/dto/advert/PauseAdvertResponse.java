package ru.akvine.wild.bot.admin.dto.advert;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PauseAdvertResponse extends SuccessfulResponse {
    private int advertId;

    @NotNull
    private AdvertStatisticDto advertStatistic;
}
