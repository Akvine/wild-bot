package ru.akvine.marketspace.bot.resolvers.controllers.views;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.resolvers.controllers.converters.StartConverter;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.services.CardAggregateService;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.domain.CardModel;
import ru.akvine.marketspace.bot.services.dto.AggregateCard;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.CHOOSE_CATEGORY_TEXT;

@Component
@RequiredArgsConstructor
public class ChooseCategoryView implements TelegramView {
    private final CardService cardService;
    private final CardAggregateService cardAggregateService;
    private final StartConverter startConverter;

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        List<CardModel> cards = cardService.list();
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
