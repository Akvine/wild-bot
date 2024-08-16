package ru.akvine.marketspace.bot.resolvers.controllers.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

@Component
public class InputNewDiscountView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
    }

    @Override
    public String getMessage(String chatId) {
        return "Введите новую скидку (без %): ";
    }

    @Override
    public ClientState byState() {
        return ClientState.INPUT_NEW_DISCOUNT_MENU;
    }
}
