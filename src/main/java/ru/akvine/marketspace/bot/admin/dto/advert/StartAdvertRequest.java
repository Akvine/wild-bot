package ru.akvine.marketspace.bot.admin.dto.advert;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@Data
@Accessors(chain = true)
public class StartAdvertRequest extends SecretRequest {
    @NotBlank
    private String advertId;
}
