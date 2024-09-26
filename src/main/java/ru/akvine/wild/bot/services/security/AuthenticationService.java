package ru.akvine.wild.bot.services.security;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.admin.SupportUserEntity;
import ru.akvine.wild.bot.exceptions.BadCredentialsException;
import ru.akvine.wild.bot.services.admin.SupportService;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final SupportService supportService;
    private final PasswordEncoder passwordEncoder;

    public SupportUserModel authenticate(String email, String password) {
        Preconditions.checkNotNull(email, "email is null");
        Preconditions.checkNotNull(password, "password is null");

        SupportUserEntity supportUser = supportService.verifyExistsByEmail(email);
        if (!passwordEncoder.matches(password, supportUser.getHash())) {
            throw new BadCredentialsException("Bad credentials!");
        }

        return new SupportUserModel(supportUser);
    }
}
