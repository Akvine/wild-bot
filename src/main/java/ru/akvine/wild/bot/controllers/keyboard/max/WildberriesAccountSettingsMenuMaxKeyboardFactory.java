package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.CHANGE_WAREHOUSE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.REVOKE_TOKEN_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class WildberriesAccountSettingsMenuMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button revokeTokenButton = MaxComponentsFactory.callbackButton(REVOKE_TOKEN_BUTTON_TEXT);
        Button changeWarehouseIdButton = MaxComponentsFactory.callbackButton(CHANGE_WAREHOUSE_BUTTON_TEXT);
        Button backButton = MaxComponentsFactory.getBackButton();

        Button[][] keyboard =
                MaxComponentsFactory.createVerticalKeyboard(revokeTokenButton, changeWarehouseIdButton, backButton);

        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU;
    }
}
