package ru.akvine.wild.bot.admin.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SendMessageRequest {
    @NotBlank
    @Size(max = 750)
    private String message;

    private List<String> chatIds;

    private List<String> usernames;
}
