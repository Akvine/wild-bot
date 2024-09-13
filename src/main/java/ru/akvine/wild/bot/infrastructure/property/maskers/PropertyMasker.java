package ru.akvine.wild.bot.infrastructure.property.maskers;

import ru.akvine.wild.bot.enums.SensitiveDataType;

public interface PropertyMasker {
    String mask(String property);

    SensitiveDataType getType();
}
