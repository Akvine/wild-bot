package ru.akvine.marketspace.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Accessors(chain = true)
public class AdvertDto {
    private String uuid;
    private int advertId;
    private String name;
    private Date changeTime;
    private AdvertStatus status;
    private AdvertType type;
    private int cpm;
    private int categoryId;
    private Integer startBudgetSum;
    private Integer checkBudgetSum;
    private LocalDateTime nextCheckDateTime;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
