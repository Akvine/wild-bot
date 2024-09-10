package ru.akvine.wild.bot.resolvers.property;

import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.services.integration.property.PropertyService;

@RequiredArgsConstructor
public abstract class AbstractPropertyParser<T> implements PropertyParser<T> {
    protected final PropertyService propertyService;
}
