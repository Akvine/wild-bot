package ru.akvine.marketspace.bot.admin.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class BlockRequest extends SecretRequest {
    private String uuid;

    private String chatId;

    private String username;
}
