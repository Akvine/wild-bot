package ru.akvine.wild.bot.admin.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

@Data
@Accessors(chain = true)
public class AddTestsResponse extends SuccessfulResponse {
    @NotBlank
    private String username;

    @NotBlank
    private String chatId;

    private int totalAvailableTestsCount;
}
