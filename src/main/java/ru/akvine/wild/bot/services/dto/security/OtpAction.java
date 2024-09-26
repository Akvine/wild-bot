package ru.akvine.wild.bot.services.dto.security;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.security.OtpActionEntity;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class OtpAction {
    private LocalDateTime startedDate;
    private LocalDateTime expiredAt;
    private int otpCountLeft;
    private Integer otpNumber;
    private LocalDateTime otpLastUpdate;
    private int otpInvalidAttemptsLeft;

    public OtpAction(OtpActionEntity entity) {
        this.startedDate = entity.getStartedDate();
        this.expiredAt = entity.getActionExpiredAt();
        this.otpNumber = entity.getOtpNumber();
        this.otpCountLeft = entity.getOtpCountLeft();
        this.otpLastUpdate = entity.getOtpLastUpdate();
        this.otpInvalidAttemptsLeft = entity.getOtpInvalidAttemptsLeft();
    }
}
