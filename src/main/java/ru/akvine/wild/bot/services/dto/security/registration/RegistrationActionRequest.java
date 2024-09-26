package ru.akvine.wild.bot.services.dto.security.registration;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class RegistrationActionRequest {
    private String sessionId;
    private String email;
    private String firstName;
    private String lastName;
    private int age;

    @Nullable
    private String otp;
    private String password;
}
