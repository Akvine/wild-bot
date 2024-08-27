package ru.akvine.wild.bot.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.akvine.wild.bot.config.TelegramFilterConfig;

@Configuration
@Import(TelegramFilterConfig.class)
public class TestMessageFilterConfig {
}
