package ru.akvine.marketspace.bot.resolvers.controllers.views;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.MALE_BUTTON_TEXT;

@Component
public class ChooseTypeView implements TelegramView {
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
        return "Запуск рекламной кампании \uD83D\uDE80: \nВыберите в какой категории будет тестироваться товар:";
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
