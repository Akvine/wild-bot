package ru.akvine.wild.bot.unit.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import ru.akvine.wild.bot.infrastructure.resilience.BulkheadFactory;
import ru.akvine.wild.bot.infrastructure.resilience.BulkheadProperties;

class BulkheadTest {

    private BulkheadProperties properties(boolean enabled, int maxCalls, int perTenant) {
        BulkheadProperties properties = new BulkheadProperties();
        properties.setEnabled(enabled);
        properties.getDefaults().setMaxConcurrentCalls(maxCalls);
        properties.getDefaults().setMaxWaitMillis(0L);
        properties.getDefaults().setMaxConcurrentCallsPerTenant(perTenant);
        return properties;
    }

    private MockClientHttpRequest request(String token) {
        MockClientHttpRequest request = new MockClientHttpRequest();
        if (token != null) {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, token);
        }
        return request;
    }

    private final ClientHttpRequestExecution ok = (req, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);

    /** Вызов, который «висит», пока не откроют защёлку */
    private CompletableFuture<Void> blockedCall(
            ClientHttpRequestInterceptor interceptor, String token, CountDownLatch entered, CountDownLatch release) {
        return CompletableFuture.runAsync(() -> {
            try {
                interceptor.intercept(request(token), new byte[0], (req, body) -> {
                    entered.countDown();
                    try {
                        release.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                });
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Test
    @DisplayName("Когда все места заняты, новый вызов получает отказ и не доходит до системы")
    void callIsRejectedWhenAllPermitsAreTaken() throws Exception {
        ClientHttpRequestInterceptor interceptor = new BulkheadFactory(properties(true, 2, 0)).interceptor("wb");
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> first = blockedCall(interceptor, null, entered, release);
        CompletableFuture<Void> second = blockedCall(interceptor, null, entered, release);
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();

        AtomicInteger reached = new AtomicInteger();
        assertThatThrownBy(() -> interceptor.intercept(request(null), new byte[0], (req, body) -> {
                    reached.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }))
                .isInstanceOf(BulkheadFullException.class);

        assertThat(reached).hasValue(0);
        release.countDown();
        first.get(5, TimeUnit.SECONDS);
        second.get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Место освобождается после вызова, в том числе завершившегося ошибкой")
    void permitIsReleasedAfterCallEvenOnFailure() throws Exception {
        ClientHttpRequestInterceptor interceptor = new BulkheadFactory(properties(true, 1, 0)).interceptor("wb");
        ClientHttpRequestExecution failing = (req, body) -> {
            throw new IOException("connection refused");
        };

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> interceptor.intercept(request(null), new byte[0], failing))
                    .isInstanceOf(IOException.class);
        }

        // если бы место не возвращалось, этот вызов получил бы BulkheadFullException
        assertThat(interceptor.intercept(request(null), new byte[0], ok).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Лимит клиента: один клиент не занимает места других, а упёршийся в лимит не тратит общее место")
    void perTenantLimitIsolatesClients() throws Exception {
        BulkheadFactory factory = new BulkheadFactory(properties(true, 10, 1));
        ClientHttpRequestInterceptor interceptor = factory.interceptor("wb");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> busyTenant = blockedCall(interceptor, "token-A", entered, release);
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();

        // второй одновременный вызов того же клиента отклоняется
        assertThatThrownBy(() -> interceptor.intercept(request("token-A"), new byte[0], ok))
                .isInstanceOf(BulkheadFullException.class);
        // другой клиент работает
        assertThat(interceptor.intercept(request("token-B"), new byte[0], ok).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        // отказ клиента A не занял общее место: занято только одно из десяти
        assertThat(factory.states().get("wb")).isEqualTo("9/10");
        // в реестре и состоянии только хэш токена, не сам токен
        assertThat(factory.states().keySet()).noneMatch(name -> name.contains("token-"));

        release.countDown();
        busyTenant.get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("При enabled=false ничего не ограничивается")
    void disabledBulkheadDoesNotLimit() throws Exception {
        BulkheadFactory factory = new BulkheadFactory(properties(false, 1, 1));
        ClientHttpRequestInterceptor interceptor = factory.interceptor("wb");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> busy = blockedCall(interceptor, "token-A", entered, release);
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(interceptor.intercept(request("token-A"), new byte[0], ok).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        release.countDown();
        busy.get(5, TimeUnit.SECONDS);
        assertThat(factory.states()).isEmpty();
        assertThat(factory.execute("wb", () -> "result")).isEqualTo("result");
    }

    @Test
    @DisplayName("execute защищает произвольный код тем же лимитом и возвращает место")
    void executeProtectsArbitraryCode() {
        BulkheadFactory factory = new BulkheadFactory(properties(true, 1, 0));

        String result = factory.execute("db", () -> {
            assertThatThrownBy(() -> factory.execute("db", () -> "nested")).isInstanceOf(BulkheadFullException.class);
            return "outer";
        });

        assertThat(result).isEqualTo("outer");
        assertThat(factory.execute("db", () -> "again")).isEqualTo("again");
        assertThat(factory.states()).containsEntry("db", "1/1");
    }

    @Test
    @DisplayName("Настройки instance переопределяют только заданные поля")
    void instanceSettingsOverrideOnlyGivenFields() {
        BulkheadProperties properties = properties(true, 25, 0);
        BulkheadProperties.Settings override = new BulkheadProperties.Settings();
        override.setMaxConcurrentCalls(3);
        properties.getInstances().put("max", override);

        assertThat(properties.resolve("max").getMaxConcurrentCalls()).isEqualTo(3);
        assertThat(properties.resolve("max").getMaxWaitMillis()).isEqualTo(0L);
        assertThat(properties.resolve("wildberries").getMaxConcurrentCalls()).isEqualTo(25);
    }
}
