package ru.akvine.wild.bot.infrastructure.http.retry;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.util.StringUtils;
import ru.akvine.wild.bot.infrastructure.http.monitoring.NamedHttpClientWrapperHelper;

/**
 * Повторяет запрос один раз, если соединение не удалось установить или оно оборвалось до ответа
 * (нет ответа, отказ в соединении, таймаут подключения). Ошибки чтения ответа и HTTP-статусы не
 * повторяются
 */
@Slf4j
public class LostConnectionRetryHandler implements HttpRequestRetryStrategy {

    private static final String CONNECT_TIMED_OUT = "CONNECT TIMED OUT";

    @Override
    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
        if (!isNeedRetryOnException(exception)) {
            logger.trace(
                    "{} ({}) was detected. isRetryNeeded=false because it is not a connect problem",
                    exception.getClass().getSimpleName(),
                    exception.getMessage());
            return false;
        }

        boolean isRetryNeeded = execCount <= 1;
        logger.warn(
                "[{}] {} ({}) was detected. isRetryNeeded={}, executionCount={}",
                NamedHttpClientWrapperHelper.getCurrentThreadLocalClientName().orElse("unnamed"),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                isRetryNeeded,
                execCount);

        return isRetryNeeded;
    }

    @Override
    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
        return false;
    }

    @Override
    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
        return TimeValue.ZERO_MILLISECONDS;
    }

    private boolean isNeedRetryOnException(IOException exception) {
        if (exception instanceof NoHttpResponseException
                || exception instanceof ConnectException
                || exception instanceof ConnectTimeoutException) {
            return true;
        }
        if (exception instanceof SocketTimeoutException && StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage().toUpperCase().contains(CONNECT_TIMED_OUT);
        }

        return false;
    }
}
