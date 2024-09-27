package ru.akvine.wild.bot.admin.dto.advert;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class RenameAdvertRequest {
    private String advertUuid;

    private Integer advertId;

    @NotBlank
    private String name;
}
