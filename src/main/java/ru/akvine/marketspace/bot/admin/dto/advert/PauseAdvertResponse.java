package ru.akvine.marketspace.bot.admin.dto.advert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SuccessfulResponse;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PauseAdvertResponse extends SuccessfulResponse {
    @NotBlank
    private String advertId;

    @NotNull
    private AdvertStatisticDto advertStatistic;
}
