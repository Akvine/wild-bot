package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

/**
 * Маскирует секрет, переиспользуя маскирование {@link PasswordPropertyMasker} (замена значения
 * целиком) — отличается только соответствующим {@link ru.akvine.wild.bot.enums.SensitiveDataType}.
 */
@Component
public class SecretPropertyMasker extends PasswordPropertyMasker {
    @Override
    public String mask(String property) {
        return super.mask(property);
    }

    @Override
    public SensitiveDataType getType() {
        return SensitiveDataType.SECRET;
    }
}
