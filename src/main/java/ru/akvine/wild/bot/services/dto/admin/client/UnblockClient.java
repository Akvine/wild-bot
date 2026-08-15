package ru.akvine.wild.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
public class UnblockClient {
    @Nullable
    private String uuid;

    @Nullable
    private String chatId;

    @Nullable
    private String username;

    @Nullable
    private BotType botType;
}
