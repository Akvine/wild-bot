package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.bot.dto.Message;

/**
 * Вспомогательные проверки над унифицированным {@link Message} — сообщением бота,
 * не зависящим от конкретной платформы (Telegram, MAX).
 */
@UtilityClass
public class BotMessageUtils {
    /**
     * Проверяет, что сообщение состоит только из стикера — без текста.
     *
     * @param message сообщение бота, может быть {@code null}
     * @return {@code true}, если сообщение не {@code null}, не содержит текста и содержит стикер
     */
    public boolean isOnlySticker(Message message) {
        if (message == null) {
            return false;
        }

        return !message.hasText() && message.hasSticker();
    }
}
