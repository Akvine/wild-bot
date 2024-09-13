package ru.akvine.wild.bot.infrastructure.property.printers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.SensitiveDataType;
import ru.akvine.wild.bot.facades.SensitivePropertyMaskersFacade;
import ru.akvine.wild.bot.infrastructure.property.detectors.SensitiveTypeDetector;
import ru.akvine.wild.bot.infrastructure.property.maskers.PropertyMasker;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogPropertiesPrinter implements PropertiesPrinter {
    private static final String BORDER = "-------------------------------";
    private static final String NEW_LINE = "\n";
    private static final String EQUAL_TO_SIGN = "=";

    private final SensitiveTypeDetector detector;
    private final SensitivePropertyMaskersFacade maskersFacade;

    @Override
    public void print(Map<String, String> properties) {
        logger.info("Start print properties by {}", LogPropertiesPrinter.class.getSimpleName());

        StringBuilder sb = new StringBuilder()
                .append(NEW_LINE)
                .append(BORDER)
                .append(NEW_LINE);

        Map<SensitiveDataType, PropertyMasker> maskers = maskersFacade.getMap();
        int count = 1;
        for (Map.Entry<String, String> propertyWithValue : properties.entrySet()) {
            SensitiveDataType dataType = detector.detect(propertyWithValue.getKey());
            if (maskers.containsKey(dataType)) {
                sb.append(count)
                        .append(". ")
                        .append(propertyWithValue.getKey())
                        .append(EQUAL_TO_SIGN)
                        .append(maskers.get(dataType).mask(properties.get(propertyWithValue.getKey())));
            } else {
                sb.append(count)
                        .append(". ")
                        .append(propertyWithValue.getKey())
                        .append(EQUAL_TO_SIGN)
                        .append(properties.get(propertyWithValue.getKey()));
            }
            count += 1;
            sb.append(NEW_LINE);
        }

        sb.append(BORDER);
        logger.info(sb.toString());
    }
}
