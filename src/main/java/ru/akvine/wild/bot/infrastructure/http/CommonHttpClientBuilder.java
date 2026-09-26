package ru.akvine.wild.bot.infrastructure.http;

import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.infrastructure.http.log.LoggingInterceptor;
import ru.akvine.wild.bot.infrastructure.http.log.LoggingInterceptorExtensions;
import ru.akvine.wild.bot.infrastructure.http.monitoring.NamedHttpClientWrapperHelper;
import ru.akvine.wild.bot.infrastructure.http.retry.LostConnectionRetryHandler;

/**
 * Собирает {@link CloseableHttpClient} (HttpClient 5) и {@link RestTemplate} поверх него: свой SSL-контекст
 * (например, из ключницы), проверка имени хоста, таймауты, пул соединений, keep-alive, прокси и
 * retry-стратегия. По умолчанию запрос повторяется при потере соединения
 * ({@link LostConnectionRetryHandler}). Если задано имя, клиент именованный - имя доступно retry-стратегии и
 * другому коду внутри клиента через {@link NamedHttpClientWrapperHelper#getCurrentThreadLocalClientName()}
 */
@Slf4j
public class CommonHttpClientBuilder {

    private SslContextProvider sslContextProvider;
    private HostnameVerifierProvider hostnameVerifierProvider;
    private SSLContext sslContext;
    private Boolean verifyHostname;

    private int connectTimeout = 10000;
    private int readTimeout = 20000;
    private Integer defaultKeepAliveTimeInMs;
    private Integer maximumKeepAliveTimeInMs;

    private int connectionPoolSize = 200;
    private boolean soKeepalive;
    private List<HttpMessageConverter<?>> messageConverters;
    private URI proxyUri;
    private HttpRequestRetryStrategy retryHandler;

    private LoggingInterceptorExtensions loggingInterceptorExtensions;

    private String name;

    private CommonHttpClientBuilder() {}

    public static CommonHttpClientBuilder createBuilder(
            SslContextProvider sslContextProvider, HostnameVerifierProvider hostnameVerifierProvider) {
        return new CommonHttpClientBuilder()
                .withSslContextProvider(sslContextProvider)
                .withHostnameVerifierProvider(hostnameVerifierProvider);
    }

    public static CommonHttpClientBuilder createBuilder(SSLContext sslContext, boolean verifyHostname) {
        return new CommonHttpClientBuilder().withVerifyHostname(verifyHostname).withSslContext(sslContext);
    }

    public CommonHttpClientBuilder withConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public CommonHttpClientBuilder withReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Повторять запросы, идемпотентные по HTTP, до {@code retryCount} раз (вместо {@link LostConnectionRetryHandler})
     */
    public CommonHttpClientBuilder withRetryCount(int retryCount) {
        Assert.isTrue(this.retryHandler == null, "Multiply RetryHandler specified");
        this.retryHandler = new DefaultHttpRequestRetryStrategy(retryCount, TimeValue.ofSeconds(1));
        return this;
    }

    public CommonHttpClientBuilder withRetryHandler(HttpRequestRetryStrategy retryHandler) {
        Assert.isTrue(this.retryHandler == null, "Multiply RetryHandler specified");
        this.retryHandler = retryHandler;
        return this;
    }

    public CommonHttpClientBuilder withProxy(URI proxyUri) {
        this.proxyUri = proxyUri;
        return this;
    }

    /** Если конвертеры не заданы, используются стандартные конвертеры {@link RestTemplate} */
    public CommonHttpClientBuilder withMessageConverters(HttpMessageConverter<?>... converters) {
        this.messageConverters = Arrays.asList(converters);
        return this;
    }

    public CommonHttpClientBuilder withConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
        return this;
    }

    public CommonHttpClientBuilder withMaximumKeepAliveTimeInMs(int maximumKeepAliveTimeInMs) {
        this.maximumKeepAliveTimeInMs = maximumKeepAliveTimeInMs;
        return this;
    }

    public CommonHttpClientBuilder withDefaultKeepAliveTimeInMs(int defaultKeepAliveTimeInMs) {
        this.defaultKeepAliveTimeInMs = defaultKeepAliveTimeInMs;
        return this;
    }

    public CommonHttpClientBuilder withSslContextProvider(SslContextProvider sslContextProvider) {
        this.sslContextProvider = sslContextProvider;
        return this;
    }

    public CommonHttpClientBuilder withHostnameVerifierProvider(HostnameVerifierProvider hostnameVerifierProvider) {
        this.hostnameVerifierProvider = hostnameVerifierProvider;
        return this;
    }

    public CommonHttpClientBuilder withSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    /** {@code false} - имя хоста в сертификате сервера не проверяется */
    public CommonHttpClientBuilder withVerifyHostname(boolean doVerify) {
        this.verifyHostname = doVerify;
        return this;
    }

    public CommonHttpClientBuilder withSoKeepalive(boolean soKeepalive) {
        this.soKeepalive = soKeepalive;
        return this;
    }

    public CommonHttpClientBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CommonHttpClientBuilder withLoggingInterceptorExtensions(
            LoggingInterceptorExtensions loggingInterceptorExtensions) {
        this.loggingInterceptorExtensions = loggingInterceptorExtensions;
        return this;
    }

    public CloseableHttpClient buildHttpClient() {
        if (verifyHostname == null && hostnameVerifierProvider == null) {
            throw new IllegalArgumentException("property verifyHostname or HostnameVerifierProvider must be specified");
        }

        if (sslContext == null && sslContextProvider == null) {
            throw new IllegalArgumentException("property sslContext or SslContextProvider must be specified");
        }

        if (maximumKeepAliveTimeInMs != null
                && maximumKeepAliveTimeInMs > 0
                && defaultKeepAliveTimeInMs != null
                && defaultKeepAliveTimeInMs > maximumKeepAliveTimeInMs) {
            throw new IllegalArgumentException(
                    "property defaultKeepAliveTimeInMs property must be less than or equal to maximumKeepAliveTimeInMs");
        }

        logger.info("Starting build CloseableHttpClient with config: {}", this);

        HostnameVerifier hostnameVerifier = this.verifyHostname != null
                ? (verifyHostname ? null : NoopHostnameVerifier.INSTANCE)
                : hostnameVerifierProvider.getHostnameVerifier();

        SSLConnectionSocketFactoryBuilder sslSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(this.sslContext != null ? this.sslContext : sslContextProvider.getSslContext());
        if (hostnameVerifier != null) {
            sslSocketFactoryBuilder.setHostnameVerifier(hostnameVerifier);
        }

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactoryBuilder.build())
                .setMaxConnTotal(connectionPoolSize)
                .setMaxConnPerRoute(connectionPoolSize)
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMilliseconds(readTimeout))
                        .setSoKeepAlive(soKeepalive)
                        .build())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                        .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
                        .build())
                .build();

        org.apache.hc.client5.http.impl.classic.HttpClientBuilder clientBuilder = HttpClients.custom()
                .useSystemProperties()
                .setConnectionManager(connectionManager)
                .setRetryStrategy(retryHandler == null ? new LostConnectionRetryHandler() : retryHandler)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout))
                        .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                        .build())
                .setProxy(
                        proxyUri == null
                                ? null
                                : new HttpHost(proxyUri.getScheme(), proxyUri.getHost(), getProxyUriPort()))
                .disableCookieManagement();

        ConnectionKeepAliveStrategy keepAliveStrategy =
                chooseConnectionKeepAliveStrategy(defaultKeepAliveTimeInMs, maximumKeepAliveTimeInMs);
        if (keepAliveStrategy != null) {
            clientBuilder.setKeepAliveStrategy(keepAliveStrategy);
        }

        CloseableHttpClient built = clientBuilder.build();

        if (!StringUtils.hasText(name)) {
            return built;
        }

        return NamedHttpClientWrapperHelper.wrap(built, name);
    }

    /**
     * Собирает RestTemplate поверх {@link #buildHttpClient()} с {@link LoggingInterceptor}. Закрывать клиент
     * при остановке приложения должен вызывающий - через {@link #buildHttpClient()} и свой
     * {@code destroyMethod}
     */
    public RestTemplate buildRestTemplate() {
        return buildRestTemplate(buildHttpClient());
    }

    public RestTemplate buildRestTemplate(CloseableHttpClient httpClient) {
        RestTemplate restClient = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        if (!CollectionUtils.isEmpty(messageConverters)) {
            restClient.setMessageConverters(messageConverters);
        }
        LoggingInterceptor loggingInterceptor = loggingInterceptorExtensions != null
                ? new LoggingInterceptor(name, loggingInterceptorExtensions)
                : new LoggingInterceptor(name);
        restClient.getInterceptors().add(loggingInterceptor);
        return restClient;
    }

    private int getProxyUriPort() {
        if (proxyUri == null) {
            return -1;
        }
        if (proxyUri.getPort() > 0) {
            return proxyUri.getPort();
        }
        return "https".equalsIgnoreCase(proxyUri.getScheme()) ? 443 : 80;
    }

    private ConnectionKeepAliveStrategy chooseConnectionKeepAliveStrategy(
            Integer defaultKeepAliveTimeInMs, Integer maximumKeepAliveTimeInMs) {
        if (defaultKeepAliveTimeInMs == null && maximumKeepAliveTimeInMs == null) {
            return null;
        }

        return connectionKeepAliveStrategy(defaultKeepAliveTimeInMs, maximumKeepAliveTimeInMs);
    }

    /**
     * Как стандартная стратегия HttpClient (берёт {@code timeout} из заголовка Keep-Alive), но с верхней границей
     * {@code maximumKeepAliveTimeInMs} и значением {@code defaultKeepAliveTimeInMs}, если заголовка нет
     */
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy(
            Integer defaultKeepAliveTimeInMs, Integer maximumKeepAliveTimeInMs) {
        return (response, context) -> {
            Iterator<HeaderElement> it = MessageSupport.iterate(response, HeaderElements.KEEP_ALIVE);
            while (it.hasNext()) {
                HeaderElement he = it.next();
                String value = he.getValue();
                if (value != null && "timeout".equalsIgnoreCase(he.getName())) {
                    try {
                        long timeoutMillis = Long.parseLong(value) * 1000;
                        return TimeValue.ofMilliseconds(
                                maximumKeepAliveTimeInMs != null
                                        ? Long.min(timeoutMillis, maximumKeepAliveTimeInMs)
                                        : timeoutMillis);
                    } catch (NumberFormatException ignored) {
                        // некорректное значение - используем значение по умолчанию
                    }
                }
            }

            return TimeValue.ofMilliseconds(
                    defaultKeepAliveTimeInMs != null ? defaultKeepAliveTimeInMs : maximumKeepAliveTimeInMs);
        };
    }

    @Override
    public String toString() {
        return "CommonHttpClientBuilder{name='" + name
                + "', verifyHostname=" + verifyHostname
                + ", connectTimeout=" + connectTimeout
                + ", readTimeout=" + readTimeout
                + ", defaultKeepAliveTimeInMs=" + defaultKeepAliveTimeInMs
                + ", maximumKeepAliveTimeInMs=" + maximumKeepAliveTimeInMs
                + ", connectionPoolSize=" + connectionPoolSize
                + ", soKeepalive=" + soKeepalive
                + ", proxyUri='" + proxyUri + "'}";
    }
}
