package ru.akvine.marketspace.bot.resolvers.controllers.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

@Component
public class FillAdvertisingView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton queryQrCodeButton = new InlineKeyboardButton();
        queryQrCodeButton.setText(QUERY_QR_CODE_BUTTON_TEXT);
        queryQrCodeButton.setCallbackData(QUERY_QR_CODE_BUTTON_TEXT);

        return KeyboardFactory.createVerticalKeyboard(queryQrCodeButton, KeyboardFactory.getBackButton());
    }

    @Override
    public String getMessage(String chatId) {
        return "Пополнить рекламный кабинет \uD83D\uDCF2: \nЗапросите QR-код для пополнения  рекламного кабинета, и бот выдаст его для  оплаты, пополнит счётчик тестов. \n Стоимость одной попытки на тест: 500 руб.";
    }

    @Override
    public ClientState byState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
