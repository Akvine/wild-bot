package ru.akvine.wild.bot.admin.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddTestsRequest {
    private String chatId;

    @NotBlank
    private String botType = "telegram";

    private int count;
}
