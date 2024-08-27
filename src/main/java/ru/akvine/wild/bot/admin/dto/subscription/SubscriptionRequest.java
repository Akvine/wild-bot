package ru.akvine.marketspace.bot.admin.dto.subscription;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class SubscriptionRequest extends SecretRequest {
    private String chatId;

    private String username;
}