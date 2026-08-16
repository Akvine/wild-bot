package ru.akvine.wild.bot.infrastructure.property.detectors;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

/**
 * Определяет тип чувствительных данных ({@link SensitiveDataType}) по имени property, ища
 * в имени одно из ключевых слов типа (например, "password", "token") без учёта регистра.
 * Используется {@code LogPropertiesPrinter}, чтобы решить, какой {@code PropertyMasker}
 * применить к значению перед выводом в лог.
 */
@Component
public class SensitiveTypeDetector {

    @Nullable
    public SensitiveDataType detect(String propertyName) {
        for (SensitiveDataType type : SensitiveDataType.values()) {
            if (propertyName.toLowerCase().contains(type.getData())) {
                return type;
            }
        }
        return null;
    }
}
