package ru.akvine.wild.bot.admin.dto.security.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthNewOtpRequest extends EmailRequest {
}
