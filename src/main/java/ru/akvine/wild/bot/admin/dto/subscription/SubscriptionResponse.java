package ru.akvine.wild.bot.admin.dto.subscription;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SubscriptionResponse extends SuccessfulResponse {
    private String chatId;

    private LocalDateTime expiresAt;

    private boolean notifiedThatExpires;

    private boolean isExpires;
}
