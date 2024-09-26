package ru.akvine.wild.bot.admin.dto.security.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@Data
public class RegistrationCheckOtpRequest extends EmailRequest {
    @NotBlank
    private String otp;
}
