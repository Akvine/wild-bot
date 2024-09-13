package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

@Component
public class PasswordPropertyMasker implements PropertyMasker {
    protected String DEFAULT_MASK_SYMBOLS = "*********";

    @Override
    public String mask(String property) {
        return DEFAULT_MASK_SYMBOLS;
    }

    @Override
    public SensitiveDataType getType() {
        return SensitiveDataType.PASSWORD;
    }
}
