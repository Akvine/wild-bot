package ru.akvine.wild.bot.facades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.resolvers.property.PropertyParser;

import java.util.Map;

@AllArgsConstructor
@Getter
public class PropertyParseFacade {
   private final Map<Class<?>, PropertyParser<?>> propertyParsers;
}
