package ru.akvine.wild.bot.admin.dto.client;

import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClientFilter {
    private Set<String> chatIds;
    private Set<String> uuids;
    private String botType;
    private Boolean deleted;
    private Boolean tokenIsNull;
    private Boolean inWhitelist;
}
