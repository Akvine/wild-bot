package ru.akvine.wild.bot.validator;

public interface Validator<T> {
    void validate(T obj);
}
