package ru.akvine.wild.bot.infrastructure.annotations;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Помечает реализацию {@code BotView}, отвечающую за отображение (текст + клавиатуру) одного
 * конкретного {@code ClientState}. Мета-аннотирована {@link Component}, поэтому помеченный
 * класс — обычный Spring-бин; {@code TelegramViewFacade} собирает все такие бины в карту
 * {@code ClientState -> BotView}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface View {}
