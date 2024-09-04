package ru.akvine.wild.bot.admin.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class SendQrCodeRequest extends SecretRequest {
    @NotBlank
    @Size(max = 255)
    private String url;
    @NotBlank
    private String chatId;
    @NotBlank
    @Size(max = 512)
    private String caption;
}
