package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.services.domain.base.SoftBean;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CardBean extends SoftBean {
    private Long id;
    private String uuid;
    @Nullable
    private String clientUuid;
    private String itemId;
    private String itemTitle;
    private String categoryTitle;
    private String categoryId;
    private String barcode;

    public CardBean(CardEntity cardEntity) {
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


