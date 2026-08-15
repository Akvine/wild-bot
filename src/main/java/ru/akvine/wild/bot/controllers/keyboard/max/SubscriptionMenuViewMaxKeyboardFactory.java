package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class SubscriptionMenuViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button paySubscriptionButton = MaxKeyboardFactory.callbackButton(PAY_SUBSCRIPTION_BUTTON_TEXT);
        Button backButton = MaxKeyboardFactory.getBackButton();

        Button[][] keyboard = MaxKeyboardFactory.createVerticalKeyboard(paySubscriptionButton, backButton);
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
