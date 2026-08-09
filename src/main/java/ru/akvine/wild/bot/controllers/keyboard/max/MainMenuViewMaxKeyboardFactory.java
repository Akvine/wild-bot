package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.ADD_SUBSCRIPTION_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.INSTRUCTIONS_FOR_USE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.TESTS_MENU;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class MainMenuViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button menuButton = MaxKeyboardFactory.callbackButton(TESTS_MENU);
        Button instructionsForUseButton = MaxKeyboardFactory.callbackButton(INSTRUCTIONS_FOR_USE_BUTTON_TEXT);
        Button addSubscriptionButton = MaxKeyboardFactory.callbackButton(ADD_SUBSCRIPTION_BUTTON_TEXT);

        Button[][] keyboard =
                MaxKeyboardFactory.createVerticalKeyboard(menuButton, instructionsForUseButton, addSubscriptionButton);
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.MAIN_MENU;
    }
}
