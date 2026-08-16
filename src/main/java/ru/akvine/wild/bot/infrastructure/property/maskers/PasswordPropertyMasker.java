package ru.akvine.wild.bot.infrastructure.property.maskers;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

/**
 * Маскирует пароль, заменяя значение целиком на фиксированную строку из звёздочек.
 * Базовый класс для остальных масок "заменить всё целиком" —
 * {@link KeyPropertyMasker}, {@link SecretPropertyMasker}, {@link TokenPropertyMasker}
 * переиспользуют её поведение через наследование.
 */
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
