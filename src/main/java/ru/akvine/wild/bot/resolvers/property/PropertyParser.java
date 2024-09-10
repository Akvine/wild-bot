package ru.akvine.wild.bot.resolvers.property;

public interface PropertyParser<T> {
    T parse(String key);

    Class<?> getType();
}
