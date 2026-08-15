package ru.akvine.wild.bot.services.dto.admin;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
public class GenerateQrCode {
    private String chatId;
    private BotType botType;
    private String url;
    private String caption;
}
