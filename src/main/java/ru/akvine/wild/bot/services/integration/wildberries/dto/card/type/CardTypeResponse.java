package ru.akvine.wild.bot.services.integration.wildberries.dto.card.type;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardTypeResponse {
    private List<String> data;
    private String error;
    private String errorText;
}
