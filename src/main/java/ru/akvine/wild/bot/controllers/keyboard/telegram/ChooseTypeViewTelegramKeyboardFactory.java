package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.MALE_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class ChooseTypeViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        InlineKeyboardButton maleButton = new InlineKeyboardButton();
        maleButton.setText(MALE_BUTTON_TEXT);
        maleButton.setCallbackData(MALE_BUTTON_TEXT);

        InlineKeyboardButton femaleButton = new InlineKeyboardButton();
        femaleButton.setText(FEMALE_BUTTON_TEXT);
        femaleButton.setCallbackData(FEMALE_BUTTON_TEXT);

        InlineKeyboardMarkup keyboardMarkup = TelegramKeyboardFactory.createVerticalKeyboard(
                maleButton, femaleButton, TelegramKeyboardFactory.getBackButton());
        return new InlineKeyboard(keyboardMarkup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
