package ru.akvine.wild.bot.infrastructure.property.detectors;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;

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
