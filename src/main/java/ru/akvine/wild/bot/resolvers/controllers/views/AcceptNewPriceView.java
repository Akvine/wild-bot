package ru.akvine.marketspace.bot.resolvers.controllers.views;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;
import ru.akvine.marketspace.bot.utils.WildberriesUtils;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.CHANGE_PRICE_BUTTON_TEXT;
import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.KEEP_PRICE_BUTTON_TEXT;

@Component
@RequiredArgsConstructor
public class AcceptNewPriceView implements TelegramView {
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton changePriceButton = new InlineKeyboardButton();
        changePriceButton.setText(CHANGE_PRICE_BUTTON_TEXT);
        changePriceButton.setCallbackData(CHANGE_PRICE_BUTTON_TEXT);

        InlineKeyboardButton keepPriceButton = new InlineKeyboardButton();
        keepPriceButton.setText(KEEP_PRICE_BUTTON_TEXT);
        keepPriceButton.setCallbackData(KEEP_PRICE_BUTTON_TEXT);

        return KeyboardFactory.createVerticalKeyboard(changePriceButton, keepPriceButton, KeyboardFactory.getBackButton());
    }

    @Override
    public String getMessage(String chatId) {
        ClientSessionData sessionData = sessionStorage.get(chatId);
        return buildMessage(sessionData.getNewCardPrice(), sessionData.getNewCardDiscount());
    }

    @Override
    public ClientState byState() {
        return ClientState.ACCEPT_NEW_PRICE_MENU;
    }

    private String buildMessage(int price, int discount) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("1. Цена без скидки: ").append(price).append("\n")
                .append("2. Скидка: ").append(discount).append("\n")
                .append("3. Цена на сайте: ").append(WildberriesUtils.calculateDiscountPrice(price, discount));
        return sb.toString();
    }
}
