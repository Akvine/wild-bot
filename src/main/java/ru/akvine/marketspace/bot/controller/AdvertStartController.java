package ru.akvine.marketspace.bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.converters.StartConverter;
import ru.akvine.marketspace.bot.controller.validatores.StartValidator;
import ru.akvine.marketspace.bot.services.CardAggregateService;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.AdvertStartService;
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

    @Value("${test.start.advert.id}")
    private String testAdvertId;

    public SendMessage getCategories(String chatId) {
        startValidator.verifyStart();
        List<CardBean> cards = cardService.list();
        List<AggregateCard> aggregateCards = cardAggregateService.aggregateByCategory(cards);
        return startConverter.buildCategories(chatId, aggregateCards);
    }

    public SendMessage startAdvertById(String chatId) {
        AdvertBean advertBean = advertStartService.startByAdvertId(testAdvertId);
        return startConverter.buildStartAdvert(chatId, advertBean);
    }

    public SendMessage startAdvert(String chatId) {
        AdvertBean advertBean = advertStartService.startRandomly();
        return startConverter.buildStartAdvert(chatId, advertBean);
    }
}
