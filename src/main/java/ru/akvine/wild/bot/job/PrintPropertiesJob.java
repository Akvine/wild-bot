package ru.akvine.wild.bot.job;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import ru.akvine.wild.bot.infrastructure.property.printers.PropertiesPrinter;
import ru.akvine.wild.bot.services.integration.property.PropertyService;

@RequiredArgsConstructor
public class PrintPropertiesJob {
    @Value("${print.properties.onlyAt.start}")
    private boolean printPropertiesOnlyAtStart;

    private final PropertyService propertyService;
    private final PropertiesPrinter propertiesPrinter;

    @PostConstruct
    private void init() {
        if (printPropertiesOnlyAtStart) {
            propertiesPrinter.print(propertyService.getProperties());
        }
    }

    @Scheduled(cron = "${print.properties.cron}")
    public void printProperties() {
        if (!printPropertiesOnlyAtStart) {
            propertiesPrinter.print(propertyService.getProperties());
        }
    }
}
