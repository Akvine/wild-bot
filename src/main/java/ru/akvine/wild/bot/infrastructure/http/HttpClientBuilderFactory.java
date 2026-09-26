package ru.akvine.wild.bot.infrastructure.http;

import javax.net.ssl.SSLContext;

/**
 * Создаёт {@link CommonHttpClientBuilder} с общими для приложения SSL-контекстом и настройкой проверки хоста.
 * Каждый именованный клиент получает свой билдер: {@code factory.createBuilder().withName("wildberries")}
 */
public class HttpClientBuilderFactory {

    private final SslContextProvider sslContextProvider;
    private final HostnameVerifierProvider hostnameVerifierProvider;

    public HttpClientBuilderFactory(
            SslContextProvider sslContextProvider, HostnameVerifierProvider hostnameVerifierProvider) {
        this.sslContextProvider = sslContextProvider;
        this.hostnameVerifierProvider = hostnameVerifierProvider;
    }

    public CommonHttpClientBuilder createBuilder() {
        return CommonHttpClientBuilder.createBuilder(sslContextProvider, hostnameVerifierProvider);
    }

    /**
     * Без ключницы: системный SSL-контекст JVM и стандартная проверка имени хоста
     */
    public static HttpClientBuilderFactory withDefaultSsl() {
        return new HttpClientBuilderFactory(HttpClientBuilderFactory::defaultSslContext, () -> null);
    }

    private static SSLContext defaultSslContext() {
        try {
            return SSLContext.getDefault();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("Can't get default SSL context", e);
        }
    }
}
