package ru.akvine.wild.bot.facades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.SensitiveDataType;
import ru.akvine.wild.bot.infrastructure.property.maskers.PropertyMasker;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SensitivePropertyMaskersFacade {
    private final Map<SensitiveDataType, PropertyMasker> map;
}
