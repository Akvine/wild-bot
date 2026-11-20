package ru.akvine.wild.bot.config;

import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.ApplicationStateMetrics;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.IntegrationMetrics;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerInterceptorFactory;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationServiceOrigin;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationServiceOrigin;

/**
 * Метрики приложения (Micrometer), которые Prometheus забирает с {@code /actuator/prometheus}. Стандартные метрики
 * (JVM, HTTP-запросы, пул соединений, задачи по расписанию, логи) Spring Boot собирает сам; здесь - то, что знает
 * только приложение:
 * <ul>
 *     <li>обращения к Telegram, Max и Wildberries ({@link IntegrationMetrics});</li>
 *     <li>показатели из базы: клиенты, подписки, кампании, очередь outbox ({@link ApplicationStateMetrics});</li>
 *     <li>загрузка пулов потоков из {@link AsyncConfig};</li>
 *     <li>состояние circuit breaker'ов внешних систем.</li>
 * </ul>
 * Обновления бота считает {@code MetricsFilter}, его добавляет в цепочку {@link BotFilterConfig}.
 * <p>
 * Всё выключается одной настройкой {@code monitoring.metrics.enabled=false}: бины отсюда не создаются, а реестры
 * метрик Spring Boot отключает строка {@code management.defaults.metrics.export.enabled} в application.properties.
 */
@Configuration
@ConditionalOnProperty(name = MetricsConfig.ENABLED_PROPERTY, havingValue = "true", matchIfMissing = true)
public class MetricsConfig {
    public static final String ENABLED_PROPERTY = "monitoring.metrics.enabled";

    // те же имена, что у circuit breaker'ов и bulkhead'ов этих систем
    public static final String WILDBERRIES = "wildberries";
    public static final String MAX = "max";
    public static final String TELEGRAM = "telegram";

    @Bean
    public IntegrationMetrics integrationMetrics(MeterRegistry meterRegistry) {
        return new IntegrationMetrics(meterRegistry);
    }

    /**
     * {@link Primary}-обёртка с замером над сервисом Telegram: её получают все, кто просит интерфейс.
     * Wildberries оборачивается в {@link ProxyConfig} - там собирается его цепочка прокси.
     */
    @Bean
    @Primary
    public TelegramIntegrationService meteredTelegramIntegrationService(
            TelegramIntegrationServiceOrigin origin, IntegrationMetrics integrationMetrics) {
        return integrationMetrics.instrument(TELEGRAM, TelegramIntegrationService.class, origin);
    }

    @Bean
    @Primary
    public MaxIntegrationService meteredMaxIntegrationService(
            MaxIntegrationServiceOrigin origin, IntegrationMetrics integrationMetrics) {
        return integrationMetrics.instrument(MAX, MaxIntegrationService.class, origin);
    }

    @Bean
    public ApplicationStateMetrics applicationStateMetrics(
            EntityManager entityManager,
            TransactionTemplate transactionTemplate,
            @Value("${monitoring.metrics.state.refresh.interval.milliseconds:60000}") long refreshIntervalMillis) {
        return new ApplicationStateMetrics(
                entityManager, transactionTemplate, Duration.ofMillis(refreshIntervalMillis));
    }

    /**
     * Пулы потоков из {@link AsyncConfig}: активные потоки, очередь, выполненные задачи. Метка {@code name} - имя бина.
     * Растущая очередь означает, что задачи приходят быстрее, чем пул их разбирает.
     */
    @Bean
    public MeterBinder executorMetrics(Map<String, ExecutorService> executors) {
        return registry -> executors.forEach(
                (name, executor) -> new ExecutorServiceMetrics(executor, name, Tags.empty()).bindTo(registry));
    }

    /**
     * Состояние и статистика circuit breaker'ов: открытый breaker означает, что внешняя система недоступна и вызовы
     * к ней отклоняются сразу.
     */
    @Bean
    public MeterBinder circuitBreakerMetrics(CircuitBreakerInterceptorFactory circuitBreakerInterceptorFactory) {
        return TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerInterceptorFactory.registry());
    }
}
