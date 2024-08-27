package ru.akvine.wild.bot.admin.converters;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.card.CardDto;
import ru.akvine.wild.bot.admin.dto.card.ListCardsRequest;
import ru.akvine.wild.bot.admin.dto.card.ListCardsResponse;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.dto.admin.card.ListCards;

import java.util.List;

@Component
public class CardConverter {
    public ListCards convertToListCards(ListCardsRequest request) {
        Preconditions.checkNotNull(request, "ListCardsRequest is null");
        return new ListCards()
                .setPage(request.getNextPage().getPage())
                .setCount(request.getNextPage().getCount())
                .setExternalTitle(request.getFilter() != null ? request.getFilter().getExternalTitle() : null)
                .setExternalId(request.getFilter() != null ? request.getFilter().getExternalId() : null)
                .setCategoryTitle(request.getFilter() != null ? request.getFilter().getCategoryTitle() : null)
                .setCategoryId(request.getFilter() != null ? request.getFilter().getCategoryId() : null);
    }

    public ListCardsResponse convertToListCardsResponse(List<CardModel> cards) {
        return new ListCardsResponse().setCards(cards.stream().map(this::buildCardDto).toList());
    }

    private CardDto buildCardDto(CardModel card) {
        return new CardDto()
                .setUuid(card.getUuid())
                .setExternalTitle(card.getExternalTitle())
                .setExternalId(card.getExternalId())
                .setCategoryTitle(card.getCategoryTitle())
                .setCategoryId(card.getCategoryId())
                .setBarcode(card.getBarcode())
                .setType(card.getCardType().getType());
    }

}
