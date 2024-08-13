package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.services.domain.base.SoftModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CardModel extends SoftModel {
    private Long id;
    private String uuid;
    @Nullable
    private String clientUuid;
    private int itemId;
    private String itemTitle;
    private String categoryTitle;
    private int categoryId;
    private String barcode;

    public CardModel(CardEntity cardEntity) {
        this.id = cardEntity.getId();
        this.uuid = cardEntity.getUuid();
        this.itemId = cardEntity.getItemId();
        this.itemTitle = cardEntity.getItemTitle();
        this.categoryTitle = cardEntity.getCategoryTitle();
        this.categoryId = cardEntity.getCategoryId();
        this.barcode = cardEntity.getBarcode();

        this.createdDate = cardEntity.getCreatedDate();
        this.updatedDate = cardEntity.getUpdatedDate();
        this.deletedDate = cardEntity.getDeletedDate();
        this.deleted = cardEntity.isDeleted();
    }
}


