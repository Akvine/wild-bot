package ru.akvine.wild.bot.services.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
public class ClientUpdate {
    private String chatId;
    private BotType botType;

    @Nullable
    private String tokenToUpdate;
}
