package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.resolvers.property.PropertyParser;

@AllArgsConstructor
@Getter
public class PropertyParseFacade {
    private final Map<Class<?>, PropertyParser<?>> propertyParsers;
}
