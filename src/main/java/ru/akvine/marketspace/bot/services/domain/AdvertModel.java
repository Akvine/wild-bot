package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;
import ru.akvine.marketspace.bot.services.domain.base.SoftModel;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Accessors(chain = true)
public class AdvertModel extends SoftModel {
    private Long id;
    private String uuid;
    private String name;
    private int externalId;
    @Nullable
    private String chatId;
    private Date changeTime;
    private AdvertStatus status;
    private AdvertType type;
    private int cpm;
    private Integer startBudgetSum;
    @Nullable
    private Integer checkBudgetSum;
    private LocalDateTime startCheckDateTime;
    @Nullable
    private LocalDateTime nextCheckDateTime;
    private LocalDateTime availableForStart;
    private boolean locked;
    private CardModel cardModel;

    public AdvertModel(AdvertEntity advertEntity) {
        this.id = advertEntity.getId();
        this.uuid = advertEntity.getUuid();
        this.name = advertEntity.getExternalTitle();
        this.locked = advertEntity.isLocked();
        this.externalId = advertEntity.getExternalId();
        this.changeTime = advertEntity.getChangeTime();
        this.status = advertEntity.getStatus();
        this.type = advertEntity.getType();
        this.cpm = advertEntity.getCpm();
        this.nextCheckDateTime = advertEntity.getNextCheckDateTime();
        this.startCheckDateTime = advertEntity.getStartCheckDateTime();
        this.startBudgetSum = advertEntity.getStartBudgetSum();
        this.checkBudgetSum = advertEntity.getCheckBudgetSum();
        this.availableForStart = advertEntity.getAvailableForStart();
        this.cardModel = new CardModel(advertEntity.getCard());

        this.createdDate = advertEntity.getCreatedDate();
        this.updatedDate = advertEntity.getUpdatedDate();
        this.deletedDate = advertEntity.getDeletedDate();
        this.deleted = advertEntity.isDeleted();
    }

    public void plusStartBudget(int value) {
        this.startBudgetSum += value;
    }

    public boolean isAvailableForStart() {
        return LocalDateTime.now().isAfter(availableForStart);
    }
}
