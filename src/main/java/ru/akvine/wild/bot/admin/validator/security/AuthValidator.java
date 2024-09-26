package ru.akvine.wild.bot.admin.validator.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;
import ru.akvine.wild.bot.validator.EmailValidator;

@Component
@RequiredArgsConstructor
public class AuthValidator {
    private final EmailValidator emailValidator;

    public void verifyAuthLogin(EmailRequest request) {
        emailValidator.validate(request.getEmail());
    }
}
