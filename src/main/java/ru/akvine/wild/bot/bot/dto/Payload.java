package ru.akvine.wild.bot.bot.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;

/**
 * Унифицированное DTO для всех ботов (Telegram, Max и т.д.)
 */
@Data
@Accessors(chain = true)
public final class Payload {
    private String chatId;

    private BotType botType;
    private BotDataType botDataType;

    private Message message;

    private String username;
    private String firstName;
    private String lastName;

    private String telegramCallbackQueryId;

    /**
     * Идентификатор обновления, уникальный в пределах бота: по нему отбрасываются повторные доставки
     * одного и того же обновления. {@code null}, если бот не сообщает такого идентификатора
     */
    private String updateId;
}
