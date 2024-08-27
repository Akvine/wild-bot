package ru.akvine.marketspace.bot.services.dto.admin.card;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class ListCards {
    @Nullable
    private String externalTitle;
    @Nullable
    private Integer externalId;
    @Nullable
    private String categoryTitle;
    @Nullable
    private Integer categoryId;
    private int page;
    private int count;
}
