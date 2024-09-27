package ru.akvine.wild.bot.admin.dto.subscription;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SubscriptionRequest {
    private String chatId;

    private String username;
}
