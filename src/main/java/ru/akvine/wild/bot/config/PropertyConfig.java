package ru.akvine.wild.bot.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.facades.SensitivePropertyMaskersFacade;
import ru.akvine.wild.bot.infrastructure.property.detectors.SensitiveTypeDetector;
import ru.akvine.wild.bot.infrastructure.property.printers.LogPropertiesPrinter;
import ru.akvine.wild.bot.infrastructure.property.printers.PropertiesPrinter;

@Configuration
public class PropertyConfig {

    @Bean
    @ConditionalOnProperty(name = "print.properties.enabled", havingValue = "true")
    public PropertiesPrinter propertiesPrinter(SensitiveTypeDetector detector,
                                               SensitivePropertyMaskersFacade facade) {
        return new LogPropertiesPrinter(detector, facade);
    }
}
