package ru.akvine.wild.bot.admin.dto.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpActionResponse {
    private String actionExpiredAt;

    private Integer otpNumber;

    private int otpCountLeft;

    private String otpLastUpdate;

    private long newOtpDelay;

    private int otpInvalidAttemptsLeft;
}
