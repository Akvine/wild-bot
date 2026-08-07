package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;

public interface BotView {
    InlineKeyboard getKeyboard(String chatId, BotType botType);

    String getMessage(String chatId);

    ClientState byState();
}
