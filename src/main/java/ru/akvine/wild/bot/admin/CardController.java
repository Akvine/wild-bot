package ru.akvine.marketspace.bot.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.marketspace.bot.admin.converters.CardConverter;
import ru.akvine.marketspace.bot.admin.dto.card.ListCardsRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;
import ru.akvine.marketspace.bot.admin.meta.CardControllerMeta;
import ru.akvine.marketspace.bot.admin.validator.AdminValidator;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.domain.CardModel;
import ru.akvine.marketspace.bot.services.dto.admin.card.ListCards;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController implements CardControllerMeta {
    private final CardService cardService;
    private final CardConverter cardConverter;
    private final AdminValidator adminValidator;

    @Override
    public Response list(@Valid ListCardsRequest request) {
        adminValidator.verifySecret(request);
        ListCards listCards = cardConverter.convertToListCards(request);
        List<CardModel> cards = cardService.list(listCards);
        return cardConverter.convertToListCardsResponse(cards);
    }
}
