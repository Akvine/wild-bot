package ru.akvine.wild.bot.admin.dto.security.registration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class RegistrationNewOtpRequest extends EmailRequest {
}
