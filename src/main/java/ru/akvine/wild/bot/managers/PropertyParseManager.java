package ru.akvine.wild.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.resolvers.property.PropertyParser;

import java.util.Map;

@AllArgsConstructor
@Getter
public class PropertyParseManager {
   private final Map<Class<?>, PropertyParser<?>> propertyParsers;
}
