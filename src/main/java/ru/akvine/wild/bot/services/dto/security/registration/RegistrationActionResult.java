package ru.akvine.wild.bot.services.dto.security.registration;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.security.RegistrationActionEntity;
import ru.akvine.wild.bot.enums.security.ActionState;
import ru.akvine.wild.bot.services.dto.security.OtpAction;

@Data
@Accessors(chain = true)
public class RegistrationActionResult {
    private ActionState state;
    private OtpAction otp;

    public RegistrationActionResult(RegistrationActionEntity registrationActionEntity) {
        this.state = registrationActionEntity.getState();
        this.otp = new OtpAction(registrationActionEntity.getOtpAction());
    }
}
