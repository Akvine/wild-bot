package ru.akvine.marketspace.bot.admin.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class SendQrCodeRequest extends SecretRequest {
    @NotBlank
    private String text;
    @NotBlank
    private String chatId;
    @NotBlank
    private String caption;
}
