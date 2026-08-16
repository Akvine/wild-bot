package ru.akvine.wild.bot.infrastructure.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает метод в {@code BotExceptionHandler} как обработчик конкретного типа исключения
 * ({@link #value()}). {@code BotExceptionFilter} при перехвате исключения рефлексией находит
 * первый метод с этой аннотацией, чьё значение соответствует классу исключения, и вызывает его.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ErrorHandler {
    Class<? extends Throwable> value();
}
