package ru.akvine.wild.bot.admin.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardDto {
    private String uuid;
    private String externalTitle;
    private int externalId;
    private String categoryTitle;
    private int categoryId;
    private String barcode;
    private String type;
}
