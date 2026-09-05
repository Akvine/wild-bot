package ru.akvine.wild.bot.services.dto.admin.client;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
public class SendMessage {
    @Nullable
    private List<String> chatIds;

    private String message;

    private BotType botType;
}
