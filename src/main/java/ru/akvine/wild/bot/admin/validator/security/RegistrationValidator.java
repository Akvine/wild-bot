package ru.akvine.wild.bot.admin.validator.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;
import ru.akvine.wild.bot.admin.dto.security.registration.RegistrationFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.registration.RegistrationPasswordValidateRequest;
import ru.akvine.wild.bot.exceptions.admin.SupportUserAlreadyExistsException;
import ru.akvine.wild.bot.services.admin.SupportService;
import ru.akvine.wild.bot.validator.EmailValidator;
import ru.akvine.wild.bot.validator.PasswordValidator;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;
    private final SupportService supportService;

    public void verifyRegistrationLogin(EmailRequest request) {
        String login = request.getEmail();
        emailValidator.validate(login);
        verifyNotExistsByLogin(request.getEmail());
    }

    public void verifyRegistrationPassword(RegistrationPasswordValidateRequest request) {
        passwordValidator.validate(request.getPassword());
    }

    public void verifyRegistrationFinish(RegistrationFinishRequest request) {
        emailValidator.validate(request.getEmail());
        passwordValidator.validate(request.getPassword());
        verifyNotExistsByLogin(request.getEmail());
    }

    public void verifyNotExistsByLogin(String login) {
        boolean exists = supportService.isExistsByEmail(login);
        if (exists) {
            throw new SupportUserAlreadyExistsException("Client with email = [" + login + "] already exists!");
        }
    }
}
