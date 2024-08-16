package ru.akvine.marketspace.bot.telegram;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.BACK_BUTTON_TEXT;

@UtilityClass
public class KeyboardFactory {
    public InlineKeyboardButton getBackButton() {
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(BACK_BUTTON_TEXT);
        backButton.setCallbackData(BACK_BUTTON_TEXT);
        return backButton;
    }

    public InlineKeyboardMarkup getBackKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = List.of(List.of(getBackButton()));
        inlineKeyboardMarkup.setKeyboard(buttons);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createVerticalKeyboard(InlineKeyboardButton... buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (InlineKeyboardButton button : buttons) {
            keyboard.add(List.of(button));
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
