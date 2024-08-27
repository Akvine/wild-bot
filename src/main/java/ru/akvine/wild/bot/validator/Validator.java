package ru.akvine.marketspace.bot.validator;

public interface Validator<T> {
    void validate(T obj);
}
