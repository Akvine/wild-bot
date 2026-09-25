package ru.akvine.wild.bot.integration.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.akvine.wild.bot.max.bot.MaxDummyBot;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;

public abstract class MaxBaseTest extends BaseTest {
    protected MaxUpdateBuilder builder;

    @MockBean
    protected MaxIntegrationService maxIntegrationService;

    @Autowired
    protected MaxDummyBot maxBot;
}
