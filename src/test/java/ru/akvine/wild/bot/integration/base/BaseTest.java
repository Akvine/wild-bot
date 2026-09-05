package ru.akvine.wild.bot.integration.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;

@SpringBootTest
public abstract class BaseTest {
    @Autowired
    protected InitMessageFilter startMessageFilter;

    @MockBean
    protected WildberriesIntegrationService wildberriesIntegrationService;
}
