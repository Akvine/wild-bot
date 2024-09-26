package ru.akvine.wild.bot.services.dto.security.access_restore;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class AccessRestoreActionRequest {
    private String sessionId;
    private String login;
    @Nullable
    private String otp;
    @Nullable
    private String password;
}
