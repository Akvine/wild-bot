package ru.akvine.wild.bot.controllers.keyboard;

import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;

public interface BotKeyboardFactory {
    InlineKeyboard create(String chatId);

    BotType getByType();

    ClientState getByState();

    default String getUniqueIdentifier() {
        return getUniqueIdentifier(getByType(), getByState());
    }

    static String getUniqueIdentifier(BotType botType, ClientState clientState) {
        return botType + "_" + clientState;
    }

}
