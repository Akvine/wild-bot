package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@UtilityClass
public class TelegramUtils {
    public boolean isSticker(TelegramData telegramData) {
        if (telegramData == null || telegramData.getData() == null) {
            return false;
        }

        return telegramData.getData().getMessage().hasSticker();
    }
}
