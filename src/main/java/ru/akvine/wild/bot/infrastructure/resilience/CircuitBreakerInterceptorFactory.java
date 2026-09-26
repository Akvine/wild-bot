package ru.akvine.wild.bot.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Выдаёт перехватчики для {@code RestTemplate} по имени внешней системы; для одного имени все
 * перехватчики делят один breaker. При {@code circuit.breaker.enabled=false} выдаёт перехватчик,
 * который ничего не делает, так что вызывающий код от настройки не зависит. Смена состояния
 * breaker'а пишется в лог.
 */
@Slf4j
public class CircuitBreakerInterceptorFactory {
    private final CircuitBreakerProperties properties;
    private final CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

    public CircuitBreakerInterceptorFactory(CircuitBreakerProperties properties) {
        this.properties = properties;
        registry.getEventPublisher()
                .onEntryAdded(event -> event.getAddedEntry()
                        .getEventPublisher()
                        .onStateTransition(transition -> logger.warn(
                                "Circuit breaker [{}] changed state: {}",
                                transition.getCircuitBreakerName(),
                                transition.getStateTransition())));
    }

    /**
     * @param name имя внешней системы, например {@code wildberries} или {@code max}
     */
    public ClientHttpRequestInterceptor create(String name) {
        if (!properties.isEnabled()) {
            return (request, body, execution) -> execution.execute(request, body);
        }
        return new CircuitBreakerInterceptor(circuitBreaker(name));
    }

    /**
     * @return состояние всех созданных breaker'ов: имя -> CLOSED / OPEN / HALF_OPEN и т.д.
     */
    public Map<String, CircuitBreaker.State> states() {
        Map<String, CircuitBreaker.State> states = new TreeMap<>();
        registry.getAllCircuitBreakers().forEach(breaker -> states.put(breaker.getName(), breaker.getState()));
        return states;
    }

    private CircuitBreaker circuitBreaker(String name) {
        return registry.circuitBreaker(name, () -> {
            CircuitBreakerProperties.Settings settings = properties.resolve(name);
            logger.info(
                    "Circuit breaker [{}] created: window = {}, minCalls = {}, failureRate = {}%, wait in open = {}s",
                    name,
                    settings.getSlidingWindowSize(),
                    settings.getMinimumNumberOfCalls(),
                    settings.getFailureRateThreshold(),
                    settings.getWaitDurationInOpenStateSeconds());
            return toConfig(settings);
        });
    }

    private CircuitBreakerConfig toConfig(CircuitBreakerProperties.Settings settings) {
        return CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(settings.getSlidingWindowSize())
                .minimumNumberOfCalls(settings.getMinimumNumberOfCalls())
                .failureRateThreshold(settings.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(settings.getWaitDurationInOpenStateSeconds()))
                .permittedNumberOfCallsInHalfOpenState(settings.getPermittedCallsInHalfOpenState())
                .slowCallDurationThreshold(Duration.ofSeconds(settings.getSlowCallDurationThresholdSeconds()))
                .slowCallRateThreshold(settings.getSlowCallRateThreshold())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }
}
