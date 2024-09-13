package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

@Component
public class IdPropertyMasker implements PropertyMasker {
    @Override
    public String mask(String property) {
        return "***";
    }

    @Override
    public SensitiveDataType getType() {
        return SensitiveDataType.ID;
    }
}
