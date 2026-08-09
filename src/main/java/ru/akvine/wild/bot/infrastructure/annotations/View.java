package ru.akvine.wild.bot.infrastructure.annotations;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface View {}
