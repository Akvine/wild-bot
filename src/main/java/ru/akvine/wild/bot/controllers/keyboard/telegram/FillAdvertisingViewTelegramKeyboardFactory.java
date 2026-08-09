package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class FillAdvertisingViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        InlineKeyboardButton queryQrCodeButton = new InlineKeyboardButton();
        queryQrCodeButton.setText(QUERY_QR_CODE_BUTTON_TEXT);
        queryQrCodeButton.setCallbackData(QUERY_QR_CODE_BUTTON_TEXT);

        InlineKeyboardMarkup keyboardMarkup = TelegramKeyboardFactory.createVerticalKeyboard(
                queryQrCodeButton, TelegramKeyboardFactory.getBackButton());
        return new InlineKeyboard(keyboardMarkup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
