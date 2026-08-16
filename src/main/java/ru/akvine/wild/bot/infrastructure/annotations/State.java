package ru.akvine.wild.bot.infrastructure.annotations;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Помечает реализацию {@code StateResolver}, отвечающую за один конкретный {@code ClientState}.
 * Мета-аннотирована {@link Component}, поэтому помеченный класс — обычный Spring-бин;
 * {@code StateResolverFacade} собирает все такие бины в карту {@code ClientState -> StateResolver}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface State {}
