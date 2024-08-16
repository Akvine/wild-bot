package ru.akvine.marketspace.bot.resolvers.controllers.view;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;

public interface TelegramView {
    InlineKeyboardMarkup getKeyboard(String chatId);

    String getMessage(String chatId);

    ClientState byState();
}
