package ru.akvine.wild.bot.integration.base;

import org.springframework.boot.test.mock.mockito.MockBean;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;

public abstract class MaxBaseTest extends BaseTest {
    @MockBean
    protected MaxIntegrationService maxIntegrationService;
}
