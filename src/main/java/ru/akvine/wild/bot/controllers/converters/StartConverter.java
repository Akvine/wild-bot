package ru.akvine.wild.bot.controllers.converters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.dto.AggregateCard;
import ru.akvine.wild.bot.services.integration.max.dto.Button;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class StartConverter {
    public InlineKeyboardMarkup buildCategories(List<AggregateCard> aggregateCards) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> buttons = aggregateCards.stream()
                .map(aggregateCard -> {
                    String text = aggregateCard.getCategoryTitle();
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(text);
                    button.setCallbackData(String.valueOf(aggregateCard.getCategoryId()));
                    return List.of(button);
                })
                .collect(Collectors.toList());

        buttons.add(List.of(TelegramKeyboardFactory.getBackButton()));
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public Button[][] buildMaxCategories(List<AggregateCard> aggregateCards) {
        Button[][] buttons = aggregateCards.stream()
                .map(aggregateCard -> new Button[] {
                    MaxComponentsFactory.callbackButton(aggregateCard.getCategoryTitle())
                            .setPayload(String.valueOf(aggregateCard.getCategoryId()))
                })
                .toArray(Button[][]::new);

        Button[][] withBackButton = Arrays.copyOf(buttons, buttons.length + 1);
        withBackButton[buttons.length] = new Button[] {MaxComponentsFactory.getBackButton()};
        return withBackButton;
    }
}
