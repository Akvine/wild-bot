package ru.akvine.wild.bot.integration.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.bot.TelegramDummyBot;

public abstract class TelegramBaseTest extends BaseTest {
    protected UpdateBuilder builder;

    @MockBean
    protected TelegramIntegrationService telegramIntegrationService;

    @Autowired
    protected TelegramDummyBot telegramBot;
}
