package ru.akvine.wild.bot.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.akvine.wild.bot.config.BotFilterConfig;

@Configuration
@Import(BotFilterConfig.class)
public class TestMessageFilterConfig {}
