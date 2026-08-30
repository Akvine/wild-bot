package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class MainMenuViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button menuButton = MaxComponentsFactory.callbackButton(TESTS_MENU);
        Button instructionsForUseButton = MaxComponentsFactory.callbackButton(INSTRUCTIONS_FOR_USE_BUTTON_TEXT);
        Button addSubscriptionButton = MaxComponentsFactory.callbackButton(ADD_SUBSCRIPTION_BUTTON_TEXT);
        Button wildberriesAccountSettingsButton =
                MaxComponentsFactory.callbackButton(WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);

        Button[][] keyboard = MaxComponentsFactory.createVerticalKeyboard(
                menuButton, instructionsForUseButton, addSubscriptionButton, wildberriesAccountSettingsButton);
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
