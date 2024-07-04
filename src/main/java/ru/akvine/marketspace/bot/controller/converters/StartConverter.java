package ru.akvine.marketspace.bot.controller.converters;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.dto.AggregateCard;

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
                    String text = aggregateCard.getCategoryTitle() + " (" + aggregateCard.getCount() + ")";
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(text);
                    button.setCallbackData(aggregateCard.getCategoryId());
                    return List.of(button);
                })
                .collect(Collectors.toList());

        inlineKeyboardMarkup.setKeyboard(buttons);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    public SendMessage buildStartAdvert(String chatId, AdvertBean startAdvertBean) {
        String message = String.format(
                "Запущена кампания с advertId = [%s], cpm = [%s], датой проверки = [%s] и бюджетом = [%s]",
                startAdvertBean.getAdvertId(),
                startAdvertBean.getCpm(),
                startAdvertBean.getNextCheckDateTime(),
                startAdvertBean.getStartBudgetSum());
        return new SendMessage(chatId, message);
    }
}
