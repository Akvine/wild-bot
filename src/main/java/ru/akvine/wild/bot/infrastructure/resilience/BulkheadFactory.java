package ru.akvine.wild.bot.infrastructure.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import ru.akvine.wild.bot.infrastructure.idempotency.Fingerprints;

/**
 * Выдаёт bulkhead'ы по имени внешней системы: перехватчики для {@code RestTemplate} ({@link #interceptor}) и
 * защиту произвольного кода ({@link #execute}). Для одного имени все вызовы делят один bulkhead. При
 * {@code bulkhead.enabled=false} ничего не ограничивает, так что вызывающий код от настройки не зависит.
 * Отказы пишутся в лог не чаще раза в {@value #REJECTION_LOG_INTERVAL_MILLIS} мс с числом отказов за это время.
 */
@Slf4j
public class BulkheadFactory {
    static final long REJECTION_LOG_INTERVAL_MILLIS = 10_000;

    private final BulkheadProperties properties;
    private final BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
    private final AtomicLong lastRejectionLogAt = new AtomicLong(0);
    private final AtomicInteger rejectionsSinceLastLog = new AtomicInteger(0);

    public BulkheadFactory(BulkheadProperties properties) {
        this.properties = properties;
        registry.getEventPublisher().onEntryAdded(event -> event.getAddedEntry()
                .getEventPublisher()
                .onCallRejected(rejected -> logRejection(rejected.getBulkheadName())));
    }

    private void logRejection(String bulkheadName) {
        rejectionsSinceLastLog.incrementAndGet();
        long now = System.currentTimeMillis();
        long last = lastRejectionLogAt.get();
        if (now - last >= REJECTION_LOG_INTERVAL_MILLIS && lastRejectionLogAt.compareAndSet(last, now)) {
            logger.warn(
                    "Bulkheads rejected {} call(s) since the last report, the latest one - [{}]",
                    rejectionsSinceLastLog.getAndSet(0),
                    bulkheadName);
        }
    }

    /**
     * Перехватчик для {@code RestTemplate}. Если для системы задан {@code max-concurrent-calls-per-tenant},
     * клиент определяется по токену в заголовке {@code Authorization} (в реестр и логи попадает только его хэш).
     *
     * @param name имя внешней системы, например {@code wildberries} или {@code max}
     */
    public ClientHttpRequestInterceptor interceptor(String name) {
        if (!properties.isEnabled()) {
            return (request, body, execution) -> execution.execute(request, body);
        }
        BulkheadProperties.Settings settings = properties.resolve(name);
        boolean perTenant =
                settings.getMaxConcurrentCallsPerTenant() != null && settings.getMaxConcurrentCallsPerTenant() > 0;
        Function<HttpRequest, Bulkhead> tenantResolver =
                perTenant ? request -> tenantBulkhead(name, request, settings) : null;
        return new BulkheadInterceptor(bulkhead(name, settings), tenantResolver);
    }

    /**
     * Выполняет код под защитой bulkhead'а системы
     *
     * @throws io.github.resilience4j.bulkhead.BulkheadFullException если все места заняты
     */
    public <T> T execute(String name, Supplier<T> action) {
        if (!properties.isEnabled()) {
            return action.get();
        }
        Bulkhead bulkhead = bulkhead(name, properties.resolve(name));
        bulkhead.acquirePermission();
        try {
            return action.get();
        } finally {
            bulkhead.onComplete();
        }
    }

    /**
     * @return состояние созданных bulkhead'ов: имя -> «свободно/всего»
     */
    public Map<String, String> states() {
        Map<String, String> states = new TreeMap<>();
        registry.getAllBulkheads()
                .forEach(bulkhead -> states.put(
                        bulkhead.getName(),
                        bulkhead.getMetrics().getAvailableConcurrentCalls() + "/"
                                + bulkhead.getMetrics().getMaxAllowedConcurrentCalls()));
        return states;
    }

    private Bulkhead bulkhead(String name, BulkheadProperties.Settings settings) {
        return registry.bulkhead(name, () -> {
            logger.info(
                    "Bulkhead [{}] created: max concurrent calls = {}, max wait = {} ms, per tenant = {}",
                    name,
                    settings.getMaxConcurrentCalls(),
                    settings.getMaxWaitMillis(),
                    settings.getMaxConcurrentCallsPerTenant());
            return config(settings.getMaxConcurrentCalls(), settings.getMaxWaitMillis());
        });
    }

    private Bulkhead tenantBulkhead(String name, HttpRequest request, BulkheadProperties.Settings settings) {
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null) {
            return null;
        }
        String tenantName = name + ":" + Fingerprints.of(token).substring(0, 12);
        return registry.bulkhead(
                tenantName, () -> config(settings.getMaxConcurrentCallsPerTenant(), settings.getMaxWaitMillis()));
    }

    private BulkheadConfig config(int maxConcurrentCalls, long maxWaitMillis) {
        return BulkheadConfig.custom()
                .maxConcurrentCalls(maxConcurrentCalls)
                .maxWaitDuration(Duration.ofMillis(maxWaitMillis))
                .build();
    }
}
