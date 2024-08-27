package ru.akvine.wild.bot.admin.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardFilter {
    private String externalTitle;

    private Integer externalId;

    private String categoryTitle;

    private Integer categoryId;
}
