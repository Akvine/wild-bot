package ru.akvine.marketspace.bot.admin.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class AddTestsRequest extends SecretRequest {
    private String chatId;

    private String username;

    private int count;
}