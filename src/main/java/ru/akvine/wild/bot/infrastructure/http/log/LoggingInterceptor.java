package ru.akvine.wild.bot.infrastructure.http.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Пишет в лог запросы и ответы {@code RestTemplate} (uri и тело) на DEBUG или TRACE, в зависимости
 * от настроек {@link LoggingInterceptorExtensions}. В сообщениях указано имя клиента
 */
@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private final String clientName;
    private final boolean logRequestInDebug;
    private final boolean logResponseInDebug;
    private final boolean logResponseStatus;

    public LoggingInterceptor(String clientName, LoggingInterceptorExtensions extensions) {
        this.clientName = clientName != null ? clientName : "unnamed";
        this.logRequestInDebug = extensions.isLogRequestInDebug();
        this.logResponseInDebug = extensions.isLogResponseInDebug();
        this.logResponseStatus = extensions.isLogResponseStatus();
    }

    public LoggingInterceptor(String clientName) {
        this.clientName = clientName != null ? clientName : "unnamed";
        this.logRequestInDebug = true;
        this.logResponseInDebug = true;
        this.logResponseStatus = false;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution)
            throws IOException {
        if (!logger.isDebugEnabled()) {
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        }

        String request = new String(bytes, StandardCharsets.UTF_8);
        if (logRequestInDebug) {
            logger.debug("[{}] RestTemplate request - uri:[{}], body:[{}]", clientName, httpRequest.getURI(), request);
        } else {
            logger.trace("[{}] RestTemplate request - uri:[{}], body:[{}]", clientName, httpRequest.getURI(), request);
        }

        LoggingHttpResponseWrapper httpResponse =
                new LoggingHttpResponseWrapper(clientHttpRequestExecution.execute(httpRequest, bytes));
        String response = new String(httpResponse.getBodyBytes(), StandardCharsets.UTF_8);
        if (logResponseStatus && !httpResponse.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
            response = String.format(
                    "%d %s: [%s]", httpResponse.getStatusCode().value(), httpResponse.getStatusText(), response);
        }
        if (logResponseInDebug) {
            logger.debug("[{}] RestTemplate response: {}", clientName, response);
        } else {
            logger.trace("[{}] RestTemplate response: {}", clientName, response);
        }
        return httpResponse;
    }
}
