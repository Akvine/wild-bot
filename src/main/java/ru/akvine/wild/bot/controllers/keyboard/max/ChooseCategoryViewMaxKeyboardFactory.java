package ru.akvine.wild.bot.controllers.keyboard.max;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.converters.StartConverter;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.services.CardAggregateService;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.dto.AggregateCard;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
@RequiredArgsConstructor
public class ChooseCategoryViewMaxKeyboardFactory implements BotKeyboardFactory {
    private final StartConverter startConverter;
    private final CardService cardService;
    private final CardAggregateService cardAggregateService;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Override
    public InlineKeyboard create(String chatId) {
        String selectedCardType = sessionStorage.get(chatId).getSelectedCardType();
        List<CardModel> cards = cardService.getByType(selectedCardType);
        List<AggregateCard> aggregateCards = cardAggregateService.aggregateByCategory(cards);
        Button[][] keyboard = startConverter.buildMaxCategories(aggregateCards);
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.CHOOSE_CATEGORY_MENU;
    }
}
