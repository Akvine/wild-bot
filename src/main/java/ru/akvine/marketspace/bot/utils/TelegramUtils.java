package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@UtilityClass
public class TelegramUtils {
    public boolean isOnlySticker(TelegramData telegramData) {
        if (telegramData == null) {
            return false;
        }

        Update update = telegramData.getData();
        if (update == null) {
            return false;
        }

        return !update.getMessage().hasText() && telegramData.getData().getMessage().hasSticker();
    }
}
