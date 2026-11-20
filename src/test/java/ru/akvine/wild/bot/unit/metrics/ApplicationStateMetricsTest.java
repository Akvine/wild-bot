package ru.akvine.wild.bot.unit.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.ApplicationStateMetrics;

/**
 * Когда и как часто опрашивается база. Сами запросы проверяются на настоящей базе в {@code MetricsEndpointTest}.
 */
@DisplayName("Показатели состояния приложения из базы")
class ApplicationStateMetricsTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);

    private void bind(Duration refreshInterval) {
        new ApplicationStateMetrics(mock(EntityManager.class), transactionTemplate, refreshInterval).bindTo(registry);
    }

    /** То, что делает Prometheus при каждом сборе: читает все показатели */
    private void readAll() {
        registry.getMeters().forEach(meter -> ((Gauge) meter).value());
    }

    @Test
    @DisplayName("Показатели есть для каждого бота и каждого статуса, даже если записей с ними нет")
    void gaugesExistForEveryKnownValue() {
        bind(Duration.ofMinutes(1));

        assertThat(registry.find(ApplicationStateMetrics.CLIENTS).gauges())
                .extracting(gauge -> gauge.getId().getTag("bot"))
                .containsExactlyInAnyOrder("telegram", "max");
        assertThat(registry.find(ApplicationStateMetrics.ADVERTS).gauges())
                .extracting(gauge -> gauge.getId().getTag("status"))
                .containsExactlyInAnyOrder("in_removal", "ready_for_start", "completed", "running", "pause");
        assertThat(registry.find(ApplicationStateMetrics.OUTBOX_MESSAGES).gauges())
                .extracting(gauge -> gauge.getId().getTag("status"))
                .containsExactlyInAnyOrder("pending", "processing", "failed");
        assertThat(registry.find(ApplicationStateMetrics.ACTIVE_SUBSCRIPTIONS).gauges())
                .hasSize(1);
        assertThat(registry.getMeters())
                .allSatisfy(meter -> assertThat(((Gauge) meter).value()).isZero());
    }

    @Test
    @DisplayName("Пока показатели никто не читает, база не опрашивается")
    void databaseIsNotAskedUntilGaugesAreRead() {
        bind(Duration.ofMinutes(1));

        verify(transactionTemplate, never()).executeWithoutResult(any());
    }

    @Test
    @DisplayName("Сколько бы раз показатели ни читали, база опрашивается один раз за интервал")
    void databaseIsAskedOncePerInterval() {
        bind(Duration.ofHours(1));

        readAll();
        readAll();

        verify(transactionTemplate, times(1)).executeWithoutResult(any());
    }

    @Test
    @DisplayName("Когда интервал истёк, чтение показателей опрашивает базу снова")
    void databaseIsAskedAgainWhenIntervalHasPassed() {
        bind(Duration.ZERO);

        readAll();
        readAll();

        verify(transactionTemplate, atLeast(2)).executeWithoutResult(any());
    }

    @Test
    @DisplayName("Сбой базы не ломает сбор метрик: отдаются прежние значения, повтор - не раньше следующего интервала")
    void databaseFailureKeepsPreviousValues() {
        doThrow(new CannotCreateTransactionException("database is down"))
                .when(transactionTemplate)
                .executeWithoutResult(any());
        bind(Duration.ofHours(1));

        readAll();
        readAll();

        assertThat(registry.getMeters())
                .allSatisfy(meter -> assertThat(((Gauge) meter).value()).isZero());
        verify(transactionTemplate, times(1)).executeWithoutResult(any());
    }
}
