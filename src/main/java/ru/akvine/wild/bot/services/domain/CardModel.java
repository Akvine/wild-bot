package ru.akvine.wild.bot.services.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.services.domain.base.SoftModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CardModel extends SoftModel {
    private Long id;
    private String uuid;
    private int externalId;
    private String externalTitle;
    private String categoryTitle;
    private int categoryId;
    private String barcode;
    private CardTypeModel cardType;

    public CardModel(CardEntity cardEntity) {
        this.id = cardEntity.getId();
        this.uuid = cardEntity.getUuid();
        this.externalId = cardEntity.getExternalId();
        this.externalTitle = cardEntity.getExternalTitle();
        this.categoryTitle = cardEntity.getCategoryTitle();
        this.categoryId = cardEntity.getCategoryId();
        this.barcode = cardEntity.getBarcode();
        this.cardType = new CardTypeModel(cardEntity.getCardType());

        this.createdDate = cardEntity.getCreatedDate();
        this.updatedDate = cardEntity.getUpdatedDate();
        this.deletedDate = cardEntity.getDeletedDate();
        this.deleted = cardEntity.isDeleted();
    }
}


