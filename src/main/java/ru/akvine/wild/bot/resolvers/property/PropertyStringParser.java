package ru.akvine.wild.bot.resolvers.property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.services.integration.property.PropertyService;

@Component
public class PropertyStringParser extends AbstractPropertyParser<String> {

    @Autowired
    public PropertyStringParser(PropertyService propertyService) {
        super(propertyService);
    }

    @Override
    public String parse(String key) {
        return propertyService.get(key);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
