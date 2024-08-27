package ru.akvine.wild.bot.admin.dto.card;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.NextPage;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class ListCardsRequest extends SecretRequest {
    private CardFilter filter;
    @NotNull
    private NextPage nextPage;
}
