package ru.akvine.wild.bot.unit.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerInterceptorFactory;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerProperties;

class CircuitBreakerInterceptorTest {

    private final MockClientHttpRequest request = new MockClientHttpRequest();
    private final AtomicInteger executions = new AtomicInteger();

    private CircuitBreakerProperties properties(boolean enabled) {
        CircuitBreakerProperties properties = new CircuitBreakerProperties();
        properties.setEnabled(enabled);
        properties.getDefaults().setSlidingWindowSize(4);
        properties.getDefaults().setMinimumNumberOfCalls(2);
        properties.getDefaults().setFailureRateThreshold(50f);
        properties.getDefaults().setWaitDurationInOpenStateSeconds(60);
        return properties;
    }

    private ClientHttpRequestExecution respondingWith(HttpStatus status) {
        return (req, body) -> {
            executions.incrementAndGet();
            return new MockClientHttpResponse(new byte[0], status);
        };
    }

    private void call(ClientHttpRequestInterceptor interceptor, ClientHttpRequestExecution execution)
            throws IOException {
        interceptor.intercept(request, new byte[0], execution);
    }

    @Test
    @DisplayName("После серии ответов 5xx breaker открывается и вызовы больше не доходят до сервера")
    void opensAfterServerErrors() throws IOException {
        CircuitBreakerInterceptorFactory factory = new CircuitBreakerInterceptorFactory(properties(true));
        ClientHttpRequestInterceptor interceptor = factory.create("wildberries");

        call(interceptor, respondingWith(HttpStatus.INTERNAL_SERVER_ERROR));
        call(interceptor, respondingWith(HttpStatus.BAD_GATEWAY));

        assertThat(factory.states()).containsEntry("wildberries", CircuitBreaker.State.OPEN);
        assertThatThrownBy(() -> call(interceptor, respondingWith(HttpStatus.OK)))
                .isInstanceOf(CallNotPermittedException.class);
        assertThat(executions).hasValue(2);
    }

    @Test
    @DisplayName("Ошибки 4xx - это ошибка клиента, а не недоступность системы: breaker не открывается")
    void clientErrorsDoNotOpenBreaker() throws IOException {
        CircuitBreakerInterceptorFactory factory = new CircuitBreakerInterceptorFactory(properties(true));
        ClientHttpRequestInterceptor interceptor = factory.create("wildberries");

        for (int i = 0; i < 10; i++) {
            call(interceptor, respondingWith(HttpStatus.UNAUTHORIZED));
        }

        assertThat(factory.states()).containsEntry("wildberries", CircuitBreaker.State.CLOSED);
        assertThat(executions).hasValue(10);
    }

    @Test
    @DisplayName("Ответы 429 в статистику не попадают")
    void tooManyRequestsAreIgnored() throws IOException {
        CircuitBreakerInterceptorFactory factory = new CircuitBreakerInterceptorFactory(properties(true));
        ClientHttpRequestInterceptor interceptor = factory.create("wildberries");

        for (int i = 0; i < 10; i++) {
            call(interceptor, respondingWith(HttpStatus.TOO_MANY_REQUESTS));
        }

        assertThat(factory.states()).containsEntry("wildberries", CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("IOException учитывается как сбой и пробрасывается дальше")
    void ioExceptionCountsAsFailureAndIsRethrown() {
        CircuitBreakerInterceptorFactory factory = new CircuitBreakerInterceptorFactory(properties(true));
        ClientHttpRequestInterceptor interceptor = factory.create("max");
        ClientHttpRequestExecution failing = (req, body) -> {
            executions.incrementAndGet();
            throw new IOException("connection refused");
        };

        assertThatThrownBy(() -> call(interceptor, failing)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> call(interceptor, failing)).isInstanceOf(IOException.class);

        assertThat(factory.states()).containsEntry("max", CircuitBreaker.State.OPEN);
        assertThatThrownBy(() -> call(interceptor, failing)).isInstanceOf(CallNotPermittedException.class);
        assertThat(executions).hasValue(2);
    }

    @Test
    @DisplayName("У каждой внешней системы свой breaker")
    void breakersAreIndependent() throws IOException {
        CircuitBreakerInterceptorFactory factory = new CircuitBreakerInterceptorFactory(properties(true));
        ClientHttpRequestInterceptor wildberries = factory.create("wildberries");
        ClientHttpRequestInterceptor max = factory.create("max");

        call(wildberries, respondingWith(HttpStatus.INTERNAL_SERVER_ERROR));
        call(wildberries, respondingWith(HttpStatus.INTERNAL_SERVER_ERROR));
        call(max, respondingWith(HttpStatus.OK));

        assertThat(factory.states())
                .containsEntry("wildberries", CircuitBreaker.State.OPEN)
                .containsEntry("max", CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("При enabled=false перехватчик ничего не блокирует")
    void disabledInterceptorPassesEverything() throws IOException {
        CircuitBreakerInterceptorFactory factory = new CircuitBreakerInterceptorFactory(properties(false));
        ClientHttpRequestInterceptor interceptor = factory.create("wildberries");

        for (int i = 0; i < 10; i++) {
            call(interceptor, respondingWith(HttpStatus.INTERNAL_SERVER_ERROR));
        }

        assertThat(executions).hasValue(10);
        assertThat(factory.states()).isEmpty();
    }

    @Test
    @DisplayName("Настройки instance переопределяют только заданные поля, остальное берётся из defaults")
    void instanceSettingsOverrideOnlyGivenFields() {
        CircuitBreakerProperties properties = properties(true);
        CircuitBreakerProperties.Settings override = new CircuitBreakerProperties.Settings();
        override.setWaitDurationInOpenStateSeconds(5);
        properties.getInstances().put("max", override);

        CircuitBreakerProperties.Settings resolved = properties.resolve("max");

        assertThat(resolved.getWaitDurationInOpenStateSeconds()).isEqualTo(5);
        assertThat(resolved.getSlidingWindowSize()).isEqualTo(4);
        assertThat(resolved.getMinimumNumberOfCalls()).isEqualTo(2);
        assertThat(properties.resolve("wildberries").getWaitDurationInOpenStateSeconds())
                .isEqualTo(60);
    }
}
