package ru.akvine.marketspace.bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.converters.StartConverter;
import ru.akvine.marketspace.bot.controller.validatores.StartValidator;
import ru.akvine.marketspace.bot.services.AdvertStartService;
import ru.akvine.marketspace.bot.services.CardAggregateService;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.domain.CardBean;
import ru.akvine.marketspace.bot.services.dto.AggregateCard;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdvertStartController {
    private final StartValidator startValidator;
    private final CardService cardService;
    private final CardAggregateService cardAggregateService;
    private final StartConverter startConverter;
    private final AdvertStartService advertStartService;

    public SendMessage getCategories(String chatId) {
        startValidator.verifyStart(chatId);
        List<CardBean> cards = cardService.list();
        List<AggregateCard> aggregateCards = cardAggregateService.aggregateByCategory(cards);
        return startConverter.buildCategories(chatId, aggregateCards);
    }

    public SendMessage startAdvert(String chatId) {
        AdvertBean advertBean = advertStartService.start(chatId);
        return startConverter.buildStartAdvert(chatId, advertBean);
    }
}
