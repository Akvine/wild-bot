package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.CHANGE_WAREHOUSE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.REVOKE_TOKEN_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class WildberriesAccountSettingsMenuTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        InlineKeyboardButton revokeTokenButton = new InlineKeyboardButton();
        revokeTokenButton.setText(REVOKE_TOKEN_BUTTON_TEXT);
        revokeTokenButton.setCallbackData(REVOKE_TOKEN_BUTTON_TEXT);

        InlineKeyboardButton changeWarehouseButton = new InlineKeyboardButton();
        changeWarehouseButton.setText(CHANGE_WAREHOUSE_BUTTON_TEXT);
        changeWarehouseButton.setCallbackData(CHANGE_WAREHOUSE_BUTTON_TEXT);

        InlineKeyboardButton backButton = TelegramKeyboardFactory.getBackButton();
        InlineKeyboardMarkup markup =
                TelegramKeyboardFactory.createVerticalKeyboard(revokeTokenButton, changeWarehouseButton, backButton);
        return new InlineKeyboard(markup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU;
    }
}
