package ru.akvine.wild.bot.services.integration.wildberries.dto.card;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardListResponse {
    private List<CardDto> cards;
}
