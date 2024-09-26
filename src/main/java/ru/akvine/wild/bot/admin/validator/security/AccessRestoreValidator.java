package ru.akvine.wild.bot.admin.validator.security;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreFinishRequest;
import ru.akvine.wild.bot.exceptions.admin.SupportUserAlreadyExistsException;
import ru.akvine.wild.bot.services.admin.SupportService;
import ru.akvine.wild.bot.validator.EmailValidator;
import ru.akvine.wild.bot.validator.PasswordValidator;

@Component
@RequiredArgsConstructor
public class AccessRestoreValidator {
    private final SupportService supportService;
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;

    public void verifyAccessRestore(EmailRequest request) {
        Preconditions.checkNotNull(request, "emailRequest is null");
        Preconditions.checkNotNull(request.getEmail(), "emailRequest.email is null");

        String email = request.getEmail();
        emailValidator.validate(email);
        verifyNotExistsByLogin(email);
    }

    public void verifyAccessRestoreFinish(AccessRestoreFinishRequest request) {
        Preconditions.checkNotNull(request, "accessRestoreFinishRequest is null");
        Preconditions.checkNotNull(request.getEmail(), "accessRestoreFinishRequest.email is null");
        Preconditions.checkNotNull(request.getPassword(), "accessRestoreFinishRequest.password is null");

        verifyNotExistsByLogin(request.getEmail());
        passwordValidator.validate(request.getPassword());
    }

    public void verifyNotExistsByLogin(String email) {
        boolean exists = supportService.isExistsByEmail(email);
        if (exists) {
            throw new SupportUserAlreadyExistsException("Support user with email = [" + email + "] already exists!");
        }
    }
}
