package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.SensitiveDataType;
import ru.akvine.wild.bot.infrastructure.property.maskers.PropertyMasker;

@AllArgsConstructor
@Getter
public class SensitivePropertyMaskersFacade {
    private final Map<SensitiveDataType, PropertyMasker> map;
}
