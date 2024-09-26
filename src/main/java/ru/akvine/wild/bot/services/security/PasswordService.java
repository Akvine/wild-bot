package ru.akvine.wild.bot.services.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    public boolean isValidPassword(SupportUserModel supportUser, String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return passwordEncoder.matches(password, supportUser.getHash());
    }

    @Nullable
    public String encodePassword(String password) {
        if (password == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
}
