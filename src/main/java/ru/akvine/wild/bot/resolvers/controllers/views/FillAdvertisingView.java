package ru.akvine.wild.bot.resolvers.controllers.views;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

@Component
public class FillAdvertisingView implements TelegramView {
    private final static String NEW_LINE = "\n";

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton queryQrCodeButton = new InlineKeyboardButton();
        queryQrCodeButton.setText(QUERY_QR_CODE_BUTTON_TEXT);
        queryQrCodeButton.setCallbackData(QUERY_QR_CODE_BUTTON_TEXT);

        return KeyboardFactory.createVerticalKeyboard(queryQrCodeButton, KeyboardFactory.getBackButton());
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Пополнить рекламный кабинет \uD83D\uDCF2:").append(NEW_LINE)
                .append("Запросите QR-код для пополнения").append(NEW_LINE)
                .append("рекламного кабинета, и бот выдаст его для  оплаты, пополнит счётчик тестов.").append(NEW_LINE)
                .append("Стоимость одной попытки на тест: 500 руб.");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
