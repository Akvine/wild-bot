package ru.akvine.wild.bot.infrastructure.monitoring.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import lombok.RequiredArgsConstructor;

/**
 * Замеряет обращения к внешним системам. Оборачивает сервис интеграции так, что каждый вызов метода его интерфейса
 * попадает в таймер {@value #REQUESTS} с метками
 * <ul>
 *     <li>{@code system} - внешняя система: wildberries, telegram, max;</li>
 *     <li>{@code operation} - имя метода интерфейса, например {@code getAdverts};</li>
 *     <li>{@code outcome} - success или error;</li>
 *     <li>{@code exception} - класс исключения, при успехе none.</li>
 * </ul>
 * Замер стоит снаружи сервиса, поэтому в него входит всё, что видит вызывающий код: сам HTTP-обмен, ожидание
 * ограничителей и повторы. Вызовы, которые сервис делает внутри себя (один перегруженный метод зовёт другой),
 * отдельно не считаются.
 */
@RequiredArgsConstructor
public class IntegrationMetrics {
    public static final String REQUESTS = "integration.requests";

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String NO_EXCEPTION = "none";

    private final MeterRegistry meterRegistry;

    /**
     * @param system имя внешней системы - то же, что у её circuit breaker'а и bulkhead'а
     * @param type публичный интерфейс сервиса интеграции
     * @param target сервис, вызовы которого нужно замерять
     * @return сервис с тем же поведением: результат и исключения передаются вызывающему коду без изменений
     */
    public <T> T instrument(String system, Class<T> type, T target) {
        if (!type.isInterface() || !Modifier.isPublic(type.getModifiers())) {
            // иначе об этом сообщит только первый вызов, и непонятным исключением рефлексии
            throw new IllegalArgumentException(type.getName() + " must be a public interface");
        }
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[] {type}, new MeteredInvocation(system, target)));
    }

    @RequiredArgsConstructor
    private final class MeteredInvocation implements InvocationHandler {
        private final String system;
        private final Object target;

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            // toString, hashCode и equals - не обращения к системе
            Timer.Sample sample = method.getDeclaringClass() == Object.class ? null : Timer.start(meterRegistry);
            String outcome = SUCCESS;
            String exception = NO_EXCEPTION;
            try {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException e) {
                // вызывающему коду нужно исключение сервиса, а не обёртка рефлексии
                Throwable failure = e.getCause();
                outcome = ERROR;
                exception = failure.getClass().getSimpleName();
                throw failure;
            } finally {
                if (sample != null) {
                    sample.stop(timer(method.getName(), outcome, exception));
                }
            }
        }

        private Timer timer(String operation, String outcome, String exception) {
            return Timer.builder(REQUESTS)
                    .description("Calls to external systems")
                    .tag("system", system)
                    .tag("operation", operation)
                    .tag("outcome", outcome)
                    .tag("exception", exception)
                    .register(meterRegistry);
        }
    }
}
