package ru.akvine.wild.bot.admin.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.CardConverter;
import ru.akvine.wild.bot.admin.dto.card.ListCardsRequest;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.meta.CardControllerMeta;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.dto.admin.card.ListCards;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController implements CardControllerMeta {
    private final CardService cardService;
    private final CardConverter cardConverter;

    @Override
    public Response list(@Valid ListCardsRequest request) {
        ListCards listCards = cardConverter.convertToListCards(request);
        List<CardModel> cards = cardService.list(listCards);
        return cardConverter.convertToListCardsResponse(cards);
    }
}
