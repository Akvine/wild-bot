package ru.akvine.wild.bot.integration.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.telegram.filter.MessageFilter;

@SpringBootTest
public abstract class BaseTest {
    @Autowired
    protected MessageFilter messageFilter;
    protected UpdateBuilder builder;

    @MockBean
    protected WildberriesIntegrationService wildberriesIntegrationService;
    @MockBean
    protected TelegramIntegrationService telegramIntegrationService;
}
