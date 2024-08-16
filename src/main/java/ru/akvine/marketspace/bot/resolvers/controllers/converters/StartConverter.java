package ru.akvine.marketspace.bot.resolvers.controllers.converters;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.services.dto.AggregateCard;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StartConverter {
    public InlineKeyboardMarkup buildCategories(List<AggregateCard> aggregateCards) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> buttons = aggregateCards
                .stream()
                .map(aggregateCard -> {
                    String text = aggregateCard.getCategoryTitle();
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(text);
                    button.setCallbackData(String.valueOf(aggregateCard.getCategoryId()));
                    return List.of(button);
                }).collect(Collectors.toList());

        buttons.add(List.of(KeyboardFactory.getBackButton()));
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public SendMessage buildStartAdvert(String chatId, AdvertModel startAdvertBean) {
        int advertId = startAdvertBean.getExternalId();
        int startCpm = startAdvertBean.getCpm();
        Integer startBudgetSum = startAdvertBean.getStartBudgetSum();
        LocalDateTime nextCheckDateTime = startAdvertBean.getNextCheckDateTime();
        String message = String.format(
                """
                           Запущена кампания с:
                           1. Advert id = %s
                           2. Начальным CPM = %s
                           3. Начальной суммой = %s руб.
                           4. Датой следующей проверки = %s 
                        """,
                advertId, startCpm, startBudgetSum, nextCheckDateTime);
        return new SendMessage(chatId, message);
    }
}
