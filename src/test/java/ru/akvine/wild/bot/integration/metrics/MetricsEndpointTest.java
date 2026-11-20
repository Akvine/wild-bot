package ru.akvine.wild.bot.integration.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.ApplicationStateMetrics;
import ru.akvine.wild.bot.integration.base.TelegramBaseTest;
import ru.akvine.wild.bot.integration.base.UpdateBuilder;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;

/**
 * Приложение целиком с настоящим реестром Prometheus: в остальных тестах Spring Boot его не создаёт, поэтому здесь
 * он включён явно ({@link AutoConfigureObservability}). Проверяется то, что увидит Prometheus.
 */
@AutoConfigureObservability(tracing = false)
@AutoConfigureMockMvc
@DisplayName("Метрики приложения по адресу /actuator/prometheus")
class MetricsEndpointTest extends TelegramBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationStateMetrics applicationStateMetrics;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private MaxIntegrationService maxIntegrationService;

    private String scrape() throws Exception {
        return mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    /** Значение метрики из ответа: строка вида {@code имя{метки} значение} */
    private static double value(String metrics, String name, String label) {
        Matcher matcher = Pattern.compile(
                        "^" + name + "\\{[^}]*" + Pattern.quote(label) + "[^}]*\\} (\\S+)$", Pattern.MULTILINE)
                .matcher(metrics);
        assertThat(matcher.find()).as("metric %s with label %s", name, label).isTrue();
        return Double.parseDouble(matcher.group(1));
    }

    @Test
    @DisplayName("Адрес открыт без входа, стандартные метрики помечены именем приложения и ролью процесса")
    void endpointIsOpenAndServesStandardMetrics() throws Exception {
        String metrics = scrape();

        assertThat(metrics)
                .contains("jvm_memory_used_bytes{")
                .contains("jdbc_connections_active{")
                .contains("logback_events_total{")
                .contains("application=\"wild-bot\"")
                .contains("role=\"all\"");
    }

    @Test
    @DisplayName("Обновление, прошедшее через бота, попадает в метрики")
    void botUpdateIsCounted() throws Exception {
        builder = new UpdateBuilder()
                .withFirstname("FirstName")
                .withLastname("SecondName")
                .withUsername("some username")
                .withText("some text")
                .withChatId("-1");

        telegramBot.onWebhookUpdateReceived(builder.build());

        assertThat(value(scrape(), "bot_updates_seconds_count", "bot=\"telegram\""))
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Показатели из базы совпадают с базой")
    void stateGaugesMatchDatabase() throws Exception {
        long telegramClients = clientRepository.findAll().stream()
                .filter(client -> !client.isDeleted() && client.getBotType() == BotType.TELEGRAM)
                .count();
        long activeSubscriptions = subscriptionRepository.findAll().stream()
                .filter(subscription -> !subscription.isExpired())
                .count();

        applicationStateMetrics.refresh();
        String metrics = scrape();

        assertThat(telegramClients).isPositive();
        assertThat(value(metrics, "bot_clients", "bot=\"telegram\"")).isEqualTo(telegramClients);
        assertThat(activeSubscriptions).isPositive();
        assertThat(value(metrics, "bot_subscriptions_active", "application=\"wild-bot\""))
                .isEqualTo(activeSubscriptions);
        assertThat(metrics).contains("bot_adverts{").contains("outbox_messages{");
        assertThat(value(metrics, "outbox_messages", "status=\"pending\"")).isNotNegative();
    }

    @Test
    @DisplayName("Сервис интеграции отдаётся с замером обращений")
    void integrationServiceIsMetered() {
        assertThat(Proxy.isProxyClass(maxIntegrationService.getClass())).isTrue();
    }
}
