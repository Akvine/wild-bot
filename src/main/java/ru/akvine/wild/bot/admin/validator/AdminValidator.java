package ru.akvine.wild.bot.admin.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;
import ru.akvine.wild.bot.exceptions.BadCredentialsException;

@Component
public class AdminValidator {
    @Value("${admin.secret}")
    private String adminSecret;

    public void verifySecret(SecretRequest request) {
        if (!adminSecret.equals(request.getSecret())) {
            throw new BadCredentialsException("Bad credentials!");
        }
    }
}
