package ru.akvine.wild.bot.admin.dto.security.access_restore;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AccessRestoreCheckOtpRequest extends EmailRequest {
    @NotBlank
    private String otp;
}
