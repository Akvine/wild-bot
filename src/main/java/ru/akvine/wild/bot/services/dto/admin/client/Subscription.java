package ru.akvine.wild.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
public class Subscription {
    @Nullable
    private String chatId;

    @Nullable
    private BotType botType;

    @Nullable
    private String username;
}
