package ru.akvine.marketspace.bot.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.akvine.marketspace.bot.config.TelegramFilterConfig;

@Configuration
@Import(TelegramFilterConfig.class)
public class TestMessageFilterConfig {
}
