package ru.akvine.wild.bot.entities.security;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OtpInfo {
    private int otpNumber;
    private String otpValue;
    private int otpInvalidAttemptsLeft;
    private Long otpLifetimeSeconds;

    public OtpInfo(int otpInvalidAttemptsLeft, Long otpLifetimeSeconds, int otpNumber, String otpValue) {
        this.otpInvalidAttemptsLeft = otpInvalidAttemptsLeft;
        this.otpLifetimeSeconds = otpLifetimeSeconds;
        this.otpNumber = otpNumber;
        this.otpValue = otpValue;
    }
}
