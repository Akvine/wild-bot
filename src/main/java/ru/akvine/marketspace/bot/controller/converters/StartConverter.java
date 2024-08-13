package ru.akvine.marketspace.bot.controller.converters;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.services.dto.AggregateCard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StartConverter {
    public SendMessage buildCategories(String chatId, List<AggregateCard> aggregateCards) {
        String message = "Выберите категорию";

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> buttons = aggregateCards
                .stream()
                .map(aggregateCard -> {
                    String text = aggregateCard.getCategoryTitle();
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(text);
                    button.setCallbackData(String.valueOf(aggregateCard.getCategoryId()));
                    return List.of(button);
                })
                .collect(Collectors.toList());

        inlineKeyboardMarkup.setKeyboard(buttons);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
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
