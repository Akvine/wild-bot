package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;
import ru.akvine.marketspace.bot.services.domain.base.SoftBean;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Accessors(chain = true)
public class AdvertBean extends SoftBean {
    private Long id;
    private String uuid;
    private String name;
    private String advertId;
    private Date changeTime;
    private AdvertStatus status;
    private AdvertType type;
    private int cpm;
    private String categoryId;
    private String itemId;
    private Integer startBudgetSum;
    @Nullable
    private Integer checkBudgetSum;
    private LocalDateTime startCheckDateTime;
    @Nullable
    private LocalDateTime nextCheckDateTime;

    public AdvertBean(AdvertEntity advertEntity) {
        this.id = advertEntity.getId();
        this.uuid = advertEntity.getUuid();
        this.name = advertEntity.getName();
        this.advertId = advertEntity.getAdvertId();
        this.itemId = advertEntity.getItemId();
        this.changeTime = advertEntity.getChangeTime();
        this.status = advertEntity.getStatus();
        this.type = advertEntity.getType();
        this.cpm = advertEntity.getCpm();
        this.categoryId = advertEntity.getCategoryId();
        this.nextCheckDateTime = advertEntity.getNextCheckDateTime();
        this.startCheckDateTime = advertEntity.getStartCheckDateTime();
        this.startBudgetSum = advertEntity.getStartBudgetSum();
        this.checkBudgetSum = advertEntity.getCheckBudgetSum();

        this.createdDate = advertEntity.getCreatedDate();
        this.updatedDate = advertEntity.getUpdatedDate();
        this.deletedDate = advertEntity.getDeletedDate();
        this.deleted = advertEntity.isDeleted();
    }
}
