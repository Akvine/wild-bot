package ru.akvine.wild.bot.infrastructure.http.retry;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;
import javax.net.ssl.SSLException;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.util.TimeValue;

/**
 * Повторяет запрос до {@code retryCount} раз при ошибках ввода-вывода (в том числе таймауте
 * чтения), кроме неизвестного хоста, отказа в соединении и ошибок TLS. HTTP-статусы не повторяются
 */
public class InterruptedRequestRetryHandler extends DefaultHttpRequestRetryStrategy {

    public InterruptedRequestRetryHandler(int retryCount) {
        super(
                retryCount,
                TimeValue.ofSeconds(1),
                List.of(UnknownHostException.class, ConnectException.class, SSLException.class),
                List.of());
    }
}
