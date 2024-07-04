package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.CardPhotoEntity;
import ru.akvine.marketspace.bot.services.domain.base.Bean;

@Data
@Accessors(chain = true)
public class CardPhotoBean extends Bean {
    private Long id;
    private String bigUrl;
    private CardBean cardBean;

    public CardPhotoBean(CardPhotoEntity cardPhotoEntity) {
        this.id = cardPhotoEntity.getId();
        this.bigUrl = cardPhotoEntity.getBigUrl();
        this.cardBean = new CardBean(cardPhotoEntity.getCardEntity());

        this.createdDate = cardPhotoEntity.getCreatedDate();
        this.updatedDate = cardPhotoEntity.getUpdatedDate();
    }
}
