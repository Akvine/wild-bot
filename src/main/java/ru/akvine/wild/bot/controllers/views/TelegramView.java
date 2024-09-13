package ru.akvine.wild.bot.controllers.views;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.enums.ClientState;

public interface TelegramView {
    InlineKeyboardMarkup getKeyboard(String chatId);

    String getMessage(String chatId);

    ClientState byState();
}
