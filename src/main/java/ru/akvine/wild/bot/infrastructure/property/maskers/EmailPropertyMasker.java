package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

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
