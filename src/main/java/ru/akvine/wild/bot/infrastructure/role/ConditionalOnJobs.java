package ru.akvine.wild.bot.infrastructure.role;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Бин создаётся только в процессах, которые запускают фоновые джобы ({@link AppRole#ALL}, {@link AppRole#WORKER})
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnAppRole({AppRole.ALL, AppRole.WORKER})
public @interface ConditionalOnJobs {}
