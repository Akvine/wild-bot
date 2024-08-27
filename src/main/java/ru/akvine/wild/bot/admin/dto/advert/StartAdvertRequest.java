package ru.akvine.wild.bot.admin.dto.advert;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

 @Data
@Accessors(chain = true)
public class StartAdvertRequest extends SecretRequest {
    private int advertId;
}
