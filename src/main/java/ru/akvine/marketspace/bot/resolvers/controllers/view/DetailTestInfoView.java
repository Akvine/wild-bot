package ru.akvine.marketspace.bot.resolvers.controllers.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

@Component
public class DetailTestInfoView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
    }

    @Override
    public String getMessage(String chatId) {
        return "Введите ID теста, чтобы получить детальную информацию по проведенному тесту: ";
    }

    @Override
    public ClientState byState() {
        return ClientState.DETAIL_TEST_INFO_MENU;
    }
}
