package ru.akvine.wild.bot.admin.dto.security.access_restore;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.dto.security.OtpActionResponse;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
public class AccessRestoreResponse extends SuccessfulResponse {
    private String state;

    private OtpActionResponse otp;
}
