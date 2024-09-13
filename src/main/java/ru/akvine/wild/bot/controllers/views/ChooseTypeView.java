package ru.akvine.wild.bot.controllers.views;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.MALE_BUTTON_TEXT;

@View
public class ChooseTypeView implements TelegramView {
    private final static String NEW_LINE = "\n";

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton maleButton = new InlineKeyboardButton();
        maleButton.setText(MALE_BUTTON_TEXT);
        maleButton.setCallbackData(MALE_BUTTON_TEXT);

        InlineKeyboardButton femaleButton = new InlineKeyboardButton();
        femaleButton.setText(FEMALE_BUTTON_TEXT);
        femaleButton.setCallbackData(FEMALE_BUTTON_TEXT);

        return KeyboardFactory.createVerticalKeyboard(maleButton, femaleButton, KeyboardFactory.getBackButton());
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Запуск рекламной кампании \uD83D\uDE80:").append(NEW_LINE)
                .append("Выберите в какой категории будет тестироваться товар:");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
