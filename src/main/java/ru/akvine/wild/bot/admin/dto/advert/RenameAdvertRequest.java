package ru.akvine.wild.bot.admin.dto.advert;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;


@Data
@Accessors(chain = true)
public class RenameAdvertRequest extends SecretRequest {
    private String advertUuid;

    private Integer advertId;

    @NotBlank
    private String name;
}
