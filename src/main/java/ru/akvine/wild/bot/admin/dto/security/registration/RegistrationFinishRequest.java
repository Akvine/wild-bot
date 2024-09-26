package ru.akvine.wild.bot.admin.dto.security.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RegistrationFinishRequest extends EmailRequest {
    @NotBlank
    @ToString.Exclude
    private String password;
}
