package ru.akvine.wild.bot.admin.dto.card;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ListCardsResponse extends SuccessfulResponse {
    @NotNull
    private List<@Valid CardDto> cards;
}
