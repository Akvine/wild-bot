package ru.akvine.marketspace.bot.admin.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;
import ru.akvine.marketspace.bot.exceptions.BadCredentialsException;

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
