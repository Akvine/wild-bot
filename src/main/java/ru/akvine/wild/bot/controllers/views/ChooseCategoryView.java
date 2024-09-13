package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.controllers.converters.StartConverter;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.services.CardAggregateService;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.dto.AggregateCard;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.CHOOSE_CATEGORY_TEXT;

@View
@RequiredArgsConstructor
public class ChooseCategoryView implements TelegramView {
    private final CardService cardService;
    private final CardAggregateService cardAggregateService;
    private final StartConverter startConverter;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        String selectedCardType = sessionStorage.get(chatId).getSelectedCardType();
        List<CardModel> cards = cardService.getByType(selectedCardType);
        List<AggregateCard> aggregateCards = cardAggregateService.aggregateByCategory(cards);
        return startConverter.buildCategories(aggregateCards);
    }

    @Override
    public String getMessage(String chatId) {
        return CHOOSE_CATEGORY_TEXT;
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_CATEGORY_MENU;
    }
}
