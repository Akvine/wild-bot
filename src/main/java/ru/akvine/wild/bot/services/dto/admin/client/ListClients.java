package ru.akvine.wild.bot.services.dto.admin.client;

import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
public class ListClients {
    private Set<String> chatIds;
    private Set<String> clientUuids;
    private BotType botType;
    private Boolean deleted;
    private Boolean tokenIsNull;
    private Boolean inWhitelist;

    private int page;
    private int count;
}
