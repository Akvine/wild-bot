package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

@Component
public class KeyPropertyMasker extends PasswordPropertyMasker {
    @Override
    public String mask(String property) {
        return super.mask(property);
    }

    @Override
    public SensitiveDataType getType() {
        return SensitiveDataType.KEY;
    }
}
