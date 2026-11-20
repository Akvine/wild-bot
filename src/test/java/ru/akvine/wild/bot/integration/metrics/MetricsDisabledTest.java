package ru.akvine.wild.bot.integration.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.akvine.wild.bot.bot.filter.MetricsFilter;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.ApplicationStateMetrics;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.IntegrationMetrics;
import ru.akvine.wild.bot.integration.base.TelegramBaseTest;
import ru.akvine.wild.bot.integration.base.UpdateBuilder;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationServiceOrigin;

/**
 * То же приложение с одной изменённой настройкой. Реестр Prometheus тестам разрешён, как в
 * {@link MetricsEndpointTest}: значит, метрик нет именно из-за настройки.
 */
@AutoConfigureObservability(tracing = false)
@AutoConfigureMockMvc
@TestPropertySource(properties = "monitoring.metrics.enabled=false")
@DisplayName("Метрики выключены настройкой monitoring.metrics.enabled=false")
class MetricsDisabledTest extends TelegramBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MaxIntegrationService maxIntegrationService;

    @Test
    @DisplayName("Адреса с метриками нет: приложение отвечает на него так же, как на любой неизвестный адрес")
    void endpointIsAbsent() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorConstants.RESOURCE_NOT_FOUND_ERROR));
    }

    @Test
    @DisplayName("Обновления бота не замеряются, бот при этом работает")
    void botUpdatesAreNotMeasured() {
        builder = new UpdateBuilder()
                .withFirstname("FirstName")
                .withLastname("SecondName")
                .withUsername("some username")
                .withText("some text")
                .withChatId("-1");

        assertThat(telegramBot.onWebhookUpdateReceived(builder.build())).isNotNull();

        assertThat(meterRegistry.find(MetricsFilter.UPDATES).timers()).isEmpty();
    }

    @Test
    @DisplayName("Сервисы интеграции отдаются без обёртки, показатели из базы не собираются")
    void nothingElseIsCollected() {
        assertThat(maxIntegrationService).isInstanceOf(MaxIntegrationServiceOrigin.class);
        assertThat(context.getBeansOfType(IntegrationMetrics.class)).isEmpty();
        assertThat(context.getBeansOfType(ApplicationStateMetrics.class)).isEmpty();
    }
}
