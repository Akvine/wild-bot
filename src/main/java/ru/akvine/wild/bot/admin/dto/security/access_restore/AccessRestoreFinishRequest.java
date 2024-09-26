package ru.akvine.wild.bot.admin.dto.security.access_restore;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@Data
@Accessors(chain = true)
public class AccessRestoreFinishRequest extends EmailRequest {
    @NotBlank
    private String password;
}
