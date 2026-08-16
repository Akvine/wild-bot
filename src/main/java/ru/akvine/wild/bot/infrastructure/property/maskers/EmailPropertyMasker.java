package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

/**
 * Маскирует email, оставляя видимыми первые 3 и последние 3 символа (например,
 * {@code "abc********xyz"}); если значение короче 4 символов — маскирует целиком.
 */
@Component
public class EmailPropertyMasker implements PropertyMasker {

    @Override
    public String mask(String property) {
        int length = property.length();
        if (length > 4) {
            return property.substring(0, 3) + "********" + property.substring(length - 3, length);
        } else {
            return "******";
        }
    }

    @Override
    public SensitiveDataType getType() {
        return SensitiveDataType.EMAIL;
    }
}
