package ru.akvine.wild.bot.services.dto.support;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SupportCreate {
    @ToString.Exclude
    private String email;
    @ToString.Exclude
    private String password;
}
