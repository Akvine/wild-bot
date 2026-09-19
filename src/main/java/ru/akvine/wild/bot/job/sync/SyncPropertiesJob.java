package ru.akvine.wild.bot.job.sync;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.wild.bot.exceptions.PropertySyncException;
import ru.akvine.wild.bot.services.integration.custodian.CustodianIntegrationService;
import ru.akvine.wild.bot.services.integration.custodian.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.custodian.dto.PropertyDto;
import ru.akvine.wild.bot.services.integration.custodian.dto.PropertyResponse;
import ru.akvine.wild.bot.services.property.PropertyCodes;
import ru.akvine.wild.bot.services.property.PropertyService;

@RequiredArgsConstructor
@Slf4j
public class SyncPropertiesJob {
    @Value("${custodian.properties.profile}")
    private String activeProfile;

    @Value("${sync.application.properties.validation.enabled}")
    private boolean validationEnabled;

    private final PropertyService propertyService;
    private final CustodianIntegrationService custodianIntegrationService;

    @Scheduled(cron = "${sync.application.properties.cron}")
    public void sync() {
        logger.info("Start sync properties from custodian service...");

        GetPropertiesRequest request = new GetPropertiesRequest(activeProfile);
        PropertyResponse propertyResponse = custodianIntegrationService.getProperties(request);

        List<PropertyDto> properties = propertyResponse.getProperties();
        for (PropertyDto property : properties) {
            String propertyKey = property.getKey();
            if (validationEnabled) {
                if (!propertyService.contains(property.getKey())) {
                    // Не должно быть расхождений в настройках между локальным хранилищем и удаленным
                    String errorMessage = String.format(
                            "Can't put property [%s] cause internal store has no property [%s]. Check properties file and external service for difference",
                            propertyKey, propertyKey);
                    throw new PropertySyncException(errorMessage);
                }

                if (PropertyCodes.isImmutable(propertyKey)) {
                    logger.info("Can't put property [{}] cause property is immutable", propertyKey);
                    continue;
                }
            }

            propertyService.put(propertyKey, property.getValue());
        }

        logger.info("Successful sync properties from custodian service...");
    }
}
