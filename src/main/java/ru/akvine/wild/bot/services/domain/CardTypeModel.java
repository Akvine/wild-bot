package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.CardTypeEntity;
import ru.akvine.marketspace.bot.services.domain.base.Model;

@Data
@Accessors(chain = true)
public class CardTypeModel extends Model {
    private Long id;
    private String type;

    public CardTypeModel(CardTypeEntity cardTypeEntity) {
        this.id = cardTypeEntity.getId();
        this.type = cardTypeEntity.getType();

        this.createdDate = cardTypeEntity.getCreatedDate();
        this.updatedDate = cardTypeEntity.getUpdatedDate();
    }
}
