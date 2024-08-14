package ru.akvine.marketspace.bot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TelegramFilterConfig.class)
public class TestMessageFilterConfig {
}
