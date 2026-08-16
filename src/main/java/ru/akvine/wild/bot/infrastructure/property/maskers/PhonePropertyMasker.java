package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

/**
 * Маскирует номер телефона, оставляя видимыми первые 4 и последние 2 символа.
 */
@Component
public class PhonePropertyMasker implements PropertyMasker {
    @Override
    public String mask(String property) {
        int length = property.length();
        return property.substring(0, 4) + "********" + property.substring(length - 2, length);
    }

    @Override
    public SensitiveDataType getType() {
        return SensitiveDataType.PHONE;
    }
}
