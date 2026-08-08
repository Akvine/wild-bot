package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.bot.dto.Message;

@UtilityClass
public class BotMessageUtils {
    public boolean isOnlySticker(Message message) {
        if (message == null) {
            return false;
        }

        return !message.hasText() && message.hasSticker();
    }
}
