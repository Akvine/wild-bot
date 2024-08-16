package ru.akvine.marketspace.bot.resolvers.controllers.views;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

@Component
public class SubscriptionMenuView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
    }

    @Override
    public String getMessage(String chatId) {
        return "Оформить подписку в два клика,  стоимость месячной подписки равно  4900 р";
    }

    @Override
    public ClientState byState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
