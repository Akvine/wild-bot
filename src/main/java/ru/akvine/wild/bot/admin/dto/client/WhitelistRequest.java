package ru.akvine.wild.bot.admin.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class WhitelistRequest extends SecretRequest {
    private String chatId;

    private String username;
}
