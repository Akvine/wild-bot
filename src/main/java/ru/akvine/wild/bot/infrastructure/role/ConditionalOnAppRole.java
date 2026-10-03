package ru.akvine.wild.bot.infrastructure.role;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

/**
 * Бин или конфигурация создаётся только в указанных ролях процесса ({@code server.app.role}, см. {@link AppRole})
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnAppRoleCondition.class)
public @interface ConditionalOnAppRole {

    AppRole[] value();
}
