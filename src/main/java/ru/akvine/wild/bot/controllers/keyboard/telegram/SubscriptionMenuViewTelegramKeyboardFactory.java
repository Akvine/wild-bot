package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class SubscriptionMenuViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        InlineKeyboardButton paySubscriptionButton = new InlineKeyboardButton();
        paySubscriptionButton.setText(PAY_SUBSCRIPTION_BUTTON_TEXT);
        paySubscriptionButton.setCallbackData(PAY_SUBSCRIPTION_BUTTON_TEXT);

        InlineKeyboardButton backButton = TelegramKeyboardFactory.getBackButton();
        InlineKeyboardMarkup markup = TelegramKeyboardFactory.createVerticalKeyboard(paySubscriptionButton, backButton);
        return new InlineKeyboard(markup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
