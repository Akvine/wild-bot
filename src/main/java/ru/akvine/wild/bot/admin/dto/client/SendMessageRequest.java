package ru.akvine.wild.bot.admin.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

import java.util.List;

@Data
@Accessors(chain = true)
public class SendMessageRequest extends SecretRequest {
    @NotBlank
    @Size(max = 750)
    private String message;

    private List<String> chatIds;

    private List<String> usernames;
}
