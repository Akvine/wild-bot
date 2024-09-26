package ru.akvine.wild.bot.services.dto.security.auth;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.security.AuthActionEntity;
import ru.akvine.wild.bot.services.dto.security.OtpAction;

@Data
@Accessors(chain = true)
public class AuthActionResult {
    private OtpAction otp;

    public AuthActionResult(AuthActionEntity authActionEntity) {
        this.otp = new OtpAction(authActionEntity.getOtpAction());
    }
}
