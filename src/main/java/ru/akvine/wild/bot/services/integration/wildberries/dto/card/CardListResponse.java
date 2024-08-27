package ru.akvine.wild.bot.services.integration.wildberries.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CardListResponse {
    private List<CardDto> cards;
}
