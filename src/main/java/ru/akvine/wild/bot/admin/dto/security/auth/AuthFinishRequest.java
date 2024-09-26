package ru.akvine.wild.bot.admin.dto.security.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthFinishRequest extends EmailRequest {
    @NotBlank
    @ToString.Exclude
    private String otp;
}
