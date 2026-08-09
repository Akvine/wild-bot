package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.CHANGE_PRICE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.KEEP_PRICE_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class IsChangePriceViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button changePriceButton = MaxKeyboardFactory.callbackButton(CHANGE_PRICE_BUTTON_TEXT);
        Button keepPriceButton = MaxKeyboardFactory.callbackButton(KEEP_PRICE_BUTTON_TEXT);

        Button[][] keyboard = MaxKeyboardFactory.createVerticalKeyboard(
                changePriceButton, keepPriceButton, MaxKeyboardFactory.getBackButton());
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.IS_CHANGE_PRICE_MENU;
    }
}
