package ru.akvine.wild.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UpdateAdvertRequest extends SecretRequest {
    private int advertId;

    private LocalDateTime availableForStart;
}
