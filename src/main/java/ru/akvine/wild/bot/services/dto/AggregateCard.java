package ru.akvine.marketspace.bot.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AggregateCard {
    private int count;
    private int categoryId;
    private String categoryTitle;
}
