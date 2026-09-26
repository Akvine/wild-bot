package ru.akvine.wild.bot.infrastructure.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import java.io.IOException;
import java.util.function.Function;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Ограничивает число одновременных HTTP-вызовов {@code RestTemplate} к одной внешней системе. Когда все
 * места заняты (медленная или зависшая система), новый вызов ждёт не дольше {@code maxWaitMillis} и получает
 * {@link BulkheadFullException}, не занимая поток приложения надолго: сбой одной системы не выедает все
 * потоки и не мешает работе с остальными.
 * <p>
 * Если задан {@code tenantBulkhead}, у каждого клиента системы есть ещё и свой лимит: один клиент с большой
 * нагрузкой не может занять все места. Место освобождается, когда получены заголовки ответа: чтение тела
 * ответа в лимит не входит.
 */
public class BulkheadInterceptor implements ClientHttpRequestInterceptor {
    private final Bulkhead systemBulkhead;
    private final Function<HttpRequest, Bulkhead> tenantBulkhead;

    /**
     * @param tenantBulkhead выдаёт bulkhead клиента для запроса или {@code null}, если отдельного лимита нет
     */
    public BulkheadInterceptor(Bulkhead systemBulkhead, Function<HttpRequest, Bulkhead> tenantBulkhead) {
        this.systemBulkhead = systemBulkhead;
        this.tenantBulkhead = tenantBulkhead;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Bulkhead tenant = tenantBulkhead == null ? null : tenantBulkhead.apply(request);
        // сначала место клиента: клиент, упёршийся в свой лимит, не занимает общее место
        if (tenant != null) {
            tenant.acquirePermission();
        }
        try {
            systemBulkhead.acquirePermission();
        } catch (BulkheadFullException e) {
            if (tenant != null) {
                tenant.onComplete();
            }
            throw e;
        }

        try {
            return execution.execute(request, body);
        } finally {
            systemBulkhead.onComplete();
            if (tenant != null) {
                tenant.onComplete();
            }
        }
    }
}
