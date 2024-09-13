package ru.akvine.wild.bot.controllers.views;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

@View
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
