package ru.akvine.wild.bot.infrastructure.monitoring.api;

import com.codahale.metrics.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Замеряет время обработки запроса и записывает его в {@link Timer} эндпоинта. Эндпоинт определяется
 * по шаблону пути, который Spring MVC кладёт в запрос при выборе контроллера, поэтому запросы к
 * {@code /x/{id}} с разными {@code id} попадают в один таймер, а запросы, не дошедшие до
 * контроллера (отклонённые Security, 404), не учитываются
 */
@Slf4j
public class ApiMetricsFilter extends OncePerRequestFilter {
    private final ApiMetricsCollector apiMetricsCollector;

    public ApiMetricsFilter(ApiMetricsCollector apiMetricsCollector) {
        this.apiMetricsCollector = apiMetricsCollector;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long startedAt = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            record(request, System.nanoTime() - startedAt);
        }
    }

    private void record(HttpServletRequest request, long elapsedNanos) {
        try {
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (pattern == null) {
                return;
            }
            Timer timer = apiMetricsCollector.getOrCreateTimer(pattern.toString());
            if (timer != null) {
                timer.update(elapsedNanos, TimeUnit.NANOSECONDS);
            }
        } catch (Exception e) {
            logger.error("Can't record endpoint metric", e);
        }
    }
}
