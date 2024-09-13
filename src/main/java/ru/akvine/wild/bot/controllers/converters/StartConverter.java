package ru.akvine.wild.bot.controllers.converters;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.services.dto.AggregateCard;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

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
}
