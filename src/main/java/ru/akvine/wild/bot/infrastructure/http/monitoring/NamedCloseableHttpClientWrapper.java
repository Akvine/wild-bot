package ru.akvine.wild.bot.infrastructure.http.monitoring;

import java.io.IOException;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

/**
 * Оборачивает http-клиент: на время выполнения каждого запроса кладёт имя клиента в
 * {@link HttpClientNameHolder}, откуда его читает
 * {@link NamedHttpClientWrapperHelper#getCurrentThreadLocalClientName()}
 */
public class NamedCloseableHttpClientWrapper extends CloseableHttpClient implements NamedHttpClient, Configurable {
    private final CloseableHttpClient wrappedClient;
    private final String name;

    public NamedCloseableHttpClientWrapper(CloseableHttpClient wrappedClient, String name) {
        this.wrappedClient = wrappedClient;
        this.name = name;
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context)
            throws IOException {
        String previous = HttpClientNameHolder.getThreadLocalName();
        try {
            HttpClientNameHolder.setThreadLocalName(name);
            return wrappedClient.execute(target, request, context);
        } finally {
            NamedHttpClientWrapperHelper.restore(previous);
        }
    }

    @Override
    public void close() throws IOException {
        wrappedClient.close();
    }

    @Override
    public void close(CloseMode closeMode) {
        wrappedClient.close(closeMode);
    }

    @Override
    public RequestConfig getConfig() {
        return wrappedClient instanceof Configurable configurable ? configurable.getConfig() : null;
    }

    @Override
    public String getHttpClientName() {
        return HttpClientNameHolder.getThreadLocalName();
    }
}
