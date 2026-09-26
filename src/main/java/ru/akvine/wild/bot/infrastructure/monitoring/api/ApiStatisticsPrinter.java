package ru.akvine.wild.bot.infrastructure.monitoring.api;

import static ru.akvine.commons.util.ScheduledExecutors.newSingleThreadScheduledExecutor;
import static ru.akvine.commons.util.Threads.newThreadFactory;

import com.codahale.metrics.Timer;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Периодически пишет в лог таблицу статистики по эндпоинтам: сколько всего запросов, среднее время
 * и сколько запросов за последнюю минуту
 */
@Slf4j(topic = "ru.akvine.wild.bot.monitoring.ApiStatistics")
public class ApiStatisticsPrinter {
    private static final String ROW_FORMAT = "%-60s | %12s | %28s | %21s%n";

    private final ApiMetricsCollector apiMetricsCollector;
    private final long intervalMinutes;
    private final ScheduledExecutorService executor;

    public ApiStatisticsPrinter(ApiMetricsCollector apiMetricsCollector, long intervalMinutes) {
        if (intervalMinutes < 1) {
            throw new IllegalArgumentException("intervalMinutes must be at least 1 but was " + intervalMinutes);
        }
        this.apiMetricsCollector = apiMetricsCollector;
        this.intervalMinutes = intervalMinutes;
        this.executor = newSingleThreadScheduledExecutor(newThreadFactory("api-statistics-printer"));
    }

    public void start() {
        executor.scheduleWithFixedDelay(this::printEndpointStatistic, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    }

    public void stop() {
        executor.shutdownNow();
    }

    public void printEndpointStatistic() {
        try {
            logger.info("Endpoint statistics:\n\n{}\n", render());
        } catch (Exception e) {
            logger.error("Can't print endpoint statistics", e);
        }
    }

    private String render() {
        StringBuilder table = new StringBuilder();
        table.append(String.format(
                ROW_FORMAT, "Endpoint", "Total count", "Average execution time (ms)", "Count for last minute"));

        List<EndpointMetric> called = apiMetricsCollector.getEndpointMetrics().stream()
                .filter(endpoint -> endpoint.getTimer() != null)
                .sorted(Comparator.comparingLong((EndpointMetric endpoint) ->
                                endpoint.getTimer().getCount())
                        .reversed())
                .toList();
        called.forEach(endpoint -> {
            Timer timer = endpoint.getTimer();
            table.append(String.format(
                    ROW_FORMAT,
                    endpoint.getName(),
                    timer.getCount(),
                    (int) (timer.getSnapshot().getMean() / 1_000_000),
                    timer.getSnapshot().size()));
        });

        apiMetricsCollector.getEndpointMetrics().stream()
                .filter(endpoint -> endpoint.getTimer() == null)
                .sorted(Comparator.comparing(EndpointMetric::getName))
                .forEach(endpoint -> table.append(String.format(ROW_FORMAT, endpoint.getName(), 0, "", "")));
        return table.toString();
    }
}
