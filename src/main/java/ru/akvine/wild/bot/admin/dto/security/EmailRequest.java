package ru.akvine.wild.bot.admin.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class EmailRequest {
    @NotBlank
    private String email;
}
