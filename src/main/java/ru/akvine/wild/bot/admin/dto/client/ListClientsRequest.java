package ru.akvine.wild.bot.admin.dto.client;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.NextPage;

@Data
@Accessors(chain = true)
public class ListClientsRequest {
    private ClientFilter filter;

    @NotNull
    private NextPage nextPage;
}
