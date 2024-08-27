package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.telegram.TelegramData;

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
