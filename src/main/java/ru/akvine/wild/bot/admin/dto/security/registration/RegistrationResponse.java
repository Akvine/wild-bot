package ru.akvine.wild.bot.admin.dto.security.registration;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.dto.security.OtpActionResponse;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
public class RegistrationResponse extends SuccessfulResponse {
    private String state;

    private OtpActionResponse otp;
}
