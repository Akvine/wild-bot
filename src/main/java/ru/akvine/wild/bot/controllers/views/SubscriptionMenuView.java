package ru.akvine.wild.bot.controllers.views;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

@View
public class SubscriptionMenuView implements TelegramView {
    private final static String NEW_LINE = "\n";

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton paySubscriptionButton = new InlineKeyboardButton();
        paySubscriptionButton.setText(PAY_SUBSCRIPTION_BUTTON_TEXT);
        paySubscriptionButton.setCallbackData(PAY_SUBSCRIPTION_BUTTON_TEXT);

        InlineKeyboardButton backButton = KeyboardFactory.getBackButton();
        return KeyboardFactory.createVerticalKeyboard(paySubscriptionButton, backButton);
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Оформить подписку в два клика.").append(NEW_LINE)
                .append("Стоимость месячной подписки составляет 4900 рублей");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
