package ru.akvine.wild.bot.infrastructure.httplogging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ни запрос, ни ответ метода (или всех методов контроллера) не логируются
 */
@DoNotLogRequest
@DoNotLogResponse
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DoNotLog {}
