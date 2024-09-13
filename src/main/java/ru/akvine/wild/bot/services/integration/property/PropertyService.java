package ru.akvine.wild.bot.services.integration.property;

import com.google.common.base.Preconditions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.exceptions.PropertiesLoadException;
import ru.akvine.wild.bot.services.integration.property.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.property.dto.PropertyDto;
import ru.akvine.wild.bot.services.integration.property.dto.PropertyResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {
    private final Map<String, String> applicationProperties = new ConcurrentHashMap<>();
    private final Environment environment;
    private final ResourceLoader resourceLoader;
    private final CustodianPropertyServiceIntegration custodianPropertyServiceIntegration;

    private static final String DEFAULT_APPLICATION_PROPERTIES_FILE_PATH = "classpath:application.properties";

    @Value("${custodian.integration.enabled}")
    private boolean propertyExternalServiceIntegrationEnabled;
    @Value("${custodian.properties.profile}")
    private String activeProfile;

    @PostConstruct
    private void init() {
        logger.info("Load properties from file...");

        String[] activeProfiles = environment.getActiveProfiles();
        Resource resource;
        Properties properties = new Properties();
        if (activeProfiles.length == 0) {
            resource = resourceLoader.getResource(DEFAULT_APPLICATION_PROPERTIES_FILE_PATH);
        } else if (activeProfiles.length == 1) {
            String file = "classpath:application-" + activeProfiles[0] + ".properties";
            resource = resourceLoader.getResource(file);
        } else {
            throw new PropertiesLoadException("Invalid count = [" + activeProfiles.length + "] of active profiles");
        }

        try {
            properties.load(resource.getInputStream());
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Some error was occurred while getInputStream() for properties.load(). Message = [%s]",
                    exception.getMessage()
            );
            throw new PropertiesLoadException(errorMessage);
        }

        for (Object key : properties.keySet()) {
            applicationProperties.put(key.toString(), properties.getProperty(key.toString()));
        }

        logger.info("Successful loaded all application properties. Count = {}", applicationProperties.size());
    }

    @Nullable
    public String getOrReturn(String key) {
        return getOrReturn(key, null);
    }

    @Nullable
    public String getOrReturn(String key, String result) {
        if (applicationProperties.containsKey(key)) {
            return result;
        }
        return applicationProperties.get(key);
    }

    public String get(String key) throws NoSuchElementException {
        Preconditions.checkNotNull(key, "key is null");
        String value = applicationProperties.get(key);
        if (value == null) {
            String errorMessage = String.format("Property with key = [%s] for current profile not defined", key);
            throw new NoSuchElementException(errorMessage);
        }
        return value;
    }

    public void sync() {
        if (!propertyExternalServiceIntegrationEnabled) {
            throw new IntegrationException(
                    "Integration with external property service is disabled. Synchronization is not possible"
            );
        }
        logger.info("Sync properties from custodian service...");

        GetPropertiesRequest request = new GetPropertiesRequest().setProfile(activeProfile);
        PropertyResponse propertyResponse = custodianPropertyServiceIntegration.getProperties(request);

        List<PropertyDto> properties = propertyResponse.getProperties();
        properties.forEach(propertyDto -> {
            if (applicationProperties.containsKey(propertyDto.getKey())) {
                applicationProperties.replace(propertyDto.getKey(), propertyDto.getValue());
            } else {
                applicationProperties.put(propertyDto.getKey(), propertyDto.getValue());
            }
        });

        logger.info("Successful sync properties from custodian service...");
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(applicationProperties);
    }
}
