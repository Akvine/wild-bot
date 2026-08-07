package ru.akvine.wild.bot.controllers.keyboard.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.CHANGE_PRICE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.KEEP_PRICE_BUTTON_TEXT;

@Component
public class IsChangePriceViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        InlineKeyboardButton changePriceButton = new InlineKeyboardButton();
        changePriceButton.setText(CHANGE_PRICE_BUTTON_TEXT);
        changePriceButton.setCallbackData(CHANGE_PRICE_BUTTON_TEXT);

        InlineKeyboardButton keepPriceButton = new InlineKeyboardButton();
        keepPriceButton.setText(KEEP_PRICE_BUTTON_TEXT);
        keepPriceButton.setCallbackData(KEEP_PRICE_BUTTON_TEXT);

        InlineKeyboardMarkup markup = TelegramKeyboardFactory.createVerticalKeyboard(changePriceButton, keepPriceButton, TelegramKeyboardFactory.getBackButton());
        return new InlineKeyboard(markup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.IS_CHANGE_PRICE_MENU;
    }
}
