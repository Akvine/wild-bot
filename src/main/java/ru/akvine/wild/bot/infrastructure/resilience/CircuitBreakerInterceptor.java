package ru.akvine.wild.bot.infrastructure.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Пропускает HTTP-вызовы {@code RestTemplate} через {@link CircuitBreaker} одной внешней системы.
 * <ul>
 *     <li>сбой - IOException (нет соединения, таймаут) и ответы 5xx;</li>
 *     <li>ответы 429 (нас ограничивает rate limit) в статистику не попадают;</li>
 *     <li>остальные ответы, включая 4xx (неверный токен, некорректный запрос), считаются успехом:
 *     система доступна, ошибка на стороне клиента;</li>
 *     <li>когда breaker открыт, вызов не выполняется, а сразу бросается {@link CallNotPermittedException}.</li>
 * </ul>
 */
public class CircuitBreakerInterceptor implements ClientHttpRequestInterceptor {
    private static final int TOO_MANY_REQUESTS = 429;

    private final CircuitBreaker circuitBreaker;

    public CircuitBreakerInterceptor(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        circuitBreaker.acquirePermission();
        long startedAt = System.nanoTime();
        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
            record(response.getStatusCode(), System.nanoTime() - startedAt);
        } catch (IOException | RuntimeException exception) {
            circuitBreaker.onError(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS, exception);
            throw exception;
        }
        return response;
    }

    private void record(HttpStatusCode status, long elapsedNanos) {
        if (status.is5xxServerError()) {
            circuitBreaker.onError(
                    elapsedNanos,
                    TimeUnit.NANOSECONDS,
                    new IOException("Server responded with status " + status.value()));
        } else if (status.value() == TOO_MANY_REQUESTS) {
            circuitBreaker.releasePermission();
        } else {
            circuitBreaker.onSuccess(elapsedNanos, TimeUnit.NANOSECONDS);
        }
    }
}
