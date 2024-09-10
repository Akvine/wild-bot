package ru.akvine.wild.bot.resolvers.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.exceptions.PropertiesLoadException;
import ru.akvine.wild.bot.services.integration.property.PropertyService;

@Component
public class PropertyFloatParser extends AbstractPropertyParser<Float> {

    @Autowired
    public PropertyFloatParser(PropertyService propertyService) {
        super(propertyService);
    }

    @Override
    public Float parse(String key) {
        try {
            return Float.parseFloat(key);
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
    public Class<Float> getType() {
        return Float.class;
    }
}
