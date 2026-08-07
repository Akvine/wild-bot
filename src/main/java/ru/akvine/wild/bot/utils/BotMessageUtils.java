package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.bot.dto.Message;

@UtilityClass
public class BotMessageUtils {
    public boolean isOnlySticker(Message message) {
        return !message.hasText() && message.hasSticker();
    }
}
