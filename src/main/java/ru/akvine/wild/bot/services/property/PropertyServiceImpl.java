package ru.akvine.wild.bot.services.property;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.exceptions.PropertiesLoadException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {
    private final Map<String, String> applicationProperties = new ConcurrentHashMap<>();
    private final Environment environment;
    private final ResourceLoader resourceLoader;

    private static final Map<Class<?>, Function<String, ?>> PARSERS = Map.of(
            Long.class, s -> s != null ? Long.parseLong(s) : 0,
            Integer.class, s -> s != null ? Integer.parseInt(s) : 0,
            String.class, s -> s,
            Boolean.class, Boolean::parseBoolean,
            Double.class, s -> s != null ? Double.parseDouble(s) : 0);
    private static final String DEFAULT_APPLICATION_PROPERTIES_FILE_PATH = "classpath:application.properties";

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
                    exception.getMessage());
            throw new PropertiesLoadException(errorMessage);
        }

        for (Object key : properties.keySet()) {
            applicationProperties.put(key.toString(), properties.getProperty(key.toString()));
        }

        logger.info("Successful loaded all application properties. Count = {}", applicationProperties.size());
    }

    @Override
    public String get(String key) throws NoSuchElementException {
        Preconditions.checkNotNull(key, "key is null");
        String value = applicationProperties.get(key);
        if (value == null) {
            String errorMessage = String.format("Property with key = [%s] for current profile not defined", key);
            throw new NoSuchElementException(errorMessage);
        }
        return value;
    }

    @Override
    public boolean contains(String propertyKey) {
        return applicationProperties.containsKey(propertyKey);
    }

    @Override
    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(applicationProperties);
    }

    @Override
    public <T> T getAs(String property, Class<T> type) {
        validate(property);
        String value = applicationProperties.get(property);
        Function<String, ?> parser = PARSERS.get(type);
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

        return type.cast(parser.apply(value));
    }

    @Override
    public List<String> getAsList(String propertyKey, String delimiter) {
        validate(propertyKey);
        if (valueIsNull(propertyKey)) {
            return new ArrayList<>();
        }
        return Arrays.stream(applicationProperties.get(propertyKey).split(delimiter))
                .toList();
    }

    @Override
    public Set<String> getAsSet(String propertyKey, String delimiter) {
        return new HashSet<>(getAsList(propertyKey, delimiter));
    }

    @Override
    public <T> List<T> getAsList(String propertyKey, String delimiter, Class<T> type) {
        if (type == String.class) {
            return (List<T>) getAsList(propertyKey, delimiter);
        }

        validate(propertyKey);
        if (valueIsNull(propertyKey)) {
            return new ArrayList<>();
        }

        Function<String, ?> parser = PARSERS.get(type);
        if (parser == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

        List<String> values = getAsList(propertyKey, delimiter);
        List<T> result = new ArrayList<>(values.size());

        for (String value : values) {
            result.add(type.cast(parser.apply(value)));
        }

        return result;
    }

    @Override
    public <T> Set<T> getAsSet(String key, String delimiter, Class<T> targetClazz) {
        return new HashSet<>(getAsList(key, delimiter, targetClazz));
    }

    @Override
    public void put(String key, String value) {
        if (applicationProperties.containsKey(key)) {
            applicationProperties.replace(key, value);
        } else {
            applicationProperties.put(key, value);
        }
    }

    private void validate(String propertyKey) {
        if (!applicationProperties.containsKey(propertyKey)) {
            throw new IllegalStateException(
                    "Internal store has no property with key = [" + propertyKey + "]. Invalid state!");
        }
    }

    private boolean valueIsNull(String propertyKey) {
        if (applicationProperties.containsKey(propertyKey)) {
            return applicationProperties.get(propertyKey) == null;
        }

        return false;
    }
}
