package ru.akvine.wild.bot.services.dto.security.auth;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class AuthActionRequest {
    private String sessionId;
    private String email;

    @Nullable
    private String password;
    @Nullable
    private String otp;
}
