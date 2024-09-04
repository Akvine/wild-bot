package ru.akvine.wild.bot.services.dto.admin;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GenerateQrCode {
    private String chatId;
    private String url;
    private String caption;
}
