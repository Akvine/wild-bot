package ru.akvine.wild.bot.admin.dto.security.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationPasswordValidateRequest {
    @NotBlank
    private String password;
}
