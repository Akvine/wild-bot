package ru.akvine.wild.bot.resolvers.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.exceptions.PropertiesLoadException;
import ru.akvine.wild.bot.services.integration.property.PropertyService;

@Component
public class PropertyIntegerParser extends AbstractPropertyParser<Integer> {
    @Autowired
    public PropertyIntegerParser(PropertyService propertyService) {
        super(propertyService);
    }

    @Override
    public Integer parse(String key) {
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException exception) {
            String errorMessage = String.format(
                    "Error while parse property with key = [%s] to type = [%s]. Message = [%s]",
                    key,
                    getType(),
                    exception.getMessage()
            );
            throw new PropertiesLoadException(errorMessage);
        }
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }
}
