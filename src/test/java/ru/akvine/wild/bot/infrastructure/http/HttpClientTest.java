package ru.akvine.wild.bot.infrastructure.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.infrastructure.http.log.LoggingHttpResponseWrapper;
import ru.akvine.wild.bot.infrastructure.http.log.LoggingInterceptor;
import ru.akvine.wild.bot.infrastructure.http.log.LoggingInterceptorExtensions;
import ru.akvine.wild.bot.infrastructure.http.monitoring.NamedHttpClient;
import ru.akvine.wild.bot.infrastructure.http.monitoring.NamedHttpClientWrapperHelper;
import ru.akvine.wild.bot.infrastructure.http.retry.InterruptedRequestRetryHandler;
import ru.akvine.wild.bot.infrastructure.http.retry.LostConnectionRetryHandler;

@DisplayName("Общий http-клиент")
class HttpClientTest {
    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> lastKeepAlive = new AtomicReference<>();
    private String keepAliveHeader;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            byte[] body = ("echo:" + new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))
                    .getBytes(StandardCharsets.UTF_8);
            if (keepAliveHeader != null) {
                exchange.getResponseHeaders().add("Keep-Alive", keepAliveHeader);
            }
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/missing", exchange -> {
            byte[] body = "nope".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private static CommonHttpClientBuilder builder() {
        return HttpClientBuilderFactory.withDefaultSsl().createBuilder();
    }

    @Test
    @DisplayName("RestTemplate из билдера ходит по http: запрос, ответ, настроенные таймауты и пул")
    void restTemplateCallsServer() {
        RestTemplate restTemplate = builder()
                .withConnectTimeout(2000)
                .withReadTimeout(2000)
                .withConnectionPoolSize(5)
                .withSoKeepalive(true)
                .withName("test-client")
                .withMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .buildRestTemplate();

        String response = restTemplate.postForObject(baseUrl + "/echo", "hello", String.class);

        assertThat(response).isEqualTo("echo:hello");
        assertThat(restTemplate.getInterceptors()).hasAtLeastOneElementOfType(LoggingInterceptor.class);
    }

    @Test
    @DisplayName(
            "Keep-Alive: заголовок учитывается с верхней границей, мусор игнорируется, по умолчанию - значение билдера")
    void keepAliveStrategy() {
        RestTemplate restTemplate = builder()
                .withDefaultKeepAliveTimeInMs(500)
                .withMaximumKeepAliveTimeInMs(1000)
                .buildRestTemplate();

        keepAliveHeader = "timeout=60";
        assertThat(restTemplate.getForObject(baseUrl + "/echo", String.class)).isEqualTo("echo:");
        keepAliveHeader = "timeout=abc";
        assertThat(restTemplate.getForObject(baseUrl + "/echo", String.class)).isEqualTo("echo:");
        keepAliveHeader = null;
        assertThat(restTemplate.getForObject(baseUrl + "/echo", String.class)).isEqualTo("echo:");

        RestTemplate onlyMax = builder().withMaximumKeepAliveTimeInMs(1000).buildRestTemplate();
        keepAliveHeader = "timeout=1";
        assertThat(onlyMax.getForObject(baseUrl + "/echo", String.class)).isEqualTo("echo:");
    }

    @Test
    @DisplayName("Именованный клиент: имя видно внутри запроса и снимается после него")
    void namedClientExposesNameDuringRequest() throws Exception {
        CloseableHttpClient client = builder().withName("wb").buildHttpClient();

        assertThat(client).isInstanceOf(NamedHttpClient.class);
        assertThat(NamedHttpClientWrapperHelper.getCurrentThreadLocalClientName())
                .isEmpty();
        client.execute(new HttpGet(baseUrl + "/echo"), response -> {
            assertThat(response.getCode()).isEqualTo(200);
            return null;
        });
        assertThat(NamedHttpClientWrapperHelper.getCurrentThreadLocalClientName())
                .isEmpty();
        assertThat(((NamedHttpClient) client).getHttpClientName()).isNull();
        client.close();
    }

    @Test
    @DisplayName("executeWithNameManually подставляет имя и возвращает прежнее")
    void executeWithNameManually() {
        String outer = NamedHttpClientWrapperHelper.executeWithNameManually("outer", () -> {
            String inner = NamedHttpClientWrapperHelper.executeWithNameManually(
                    "inner", () -> NamedHttpClientWrapperHelper.getCurrentThreadLocalClientName()
                            .orElseThrow());
            assertThat(inner).isEqualTo("inner");
            return NamedHttpClientWrapperHelper.getCurrentThreadLocalClientName()
                    .orElseThrow();
        });

        assertThat(outer).isEqualTo("outer");
        assertThat(NamedHttpClientWrapperHelper.getCurrentThreadLocalClientName())
                .isEmpty();

        boolean[] ran = {false};
        NamedHttpClientWrapperHelper.executeWithNameManually("runnable", () -> ran[0] = true);
        assertThat(ran[0]).isTrue();
    }

    @Test
    @DisplayName("Обязательные настройки проверяются при сборке клиента")
    void validatesConfiguration() throws Exception {
        SSLContext ssl = SSLContext.getDefault();

        assertThatThrownBy(() -> CommonHttpClientBuilder.createBuilder(ssl, true)
                        .withSslContext(null)
                        .buildHttpClient())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sslContext");
        assertThatThrownBy(() -> CommonHttpClientBuilder.createBuilder((SSLContext) null, true)
                        .withVerifyHostname(true)
                        .buildHttpClient())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CommonHttpClientBuilder.createBuilder(null, null)
                        .withSslContext(ssl)
                        .buildHttpClient())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("verifyHostname");
        assertThatThrownBy(() -> builder()
                        .withDefaultKeepAliveTimeInMs(2000)
                        .withMaximumKeepAliveTimeInMs(1000)
                        .buildHttpClient())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("defaultKeepAliveTimeInMs");
    }

    @Test
    @DisplayName("Retry-стратегию можно задать только один раз")
    void retryHandlerOnlyOnce() {
        assertThatThrownBy(() -> builder().withRetryCount(2).withRetryHandler(new LostConnectionRetryHandler()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> builder()
                        .withRetryHandler(new LostConnectionRetryHandler())
                        .withRetryCount(2))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(builder().withRetryCount(2).buildHttpClient()).isNotNull();
    }

    @Test
    @DisplayName("Верификатор имени хоста: отключённая проверка, провайдеры SSL и прокси")
    void sslAndProxyOptions() throws Exception {
        assertThat(CommonHttpClientBuilder.createBuilder(SSLContext.getDefault(), false)
                        .withProxy(URI.create("http://127.0.0.1:3128"))
                        .buildHttpClient())
                .isNotNull();
        assertThat(CommonHttpClientBuilder.createBuilder(SSLContext.getDefault(), true)
                        .withProxy(URI.create("https://proxy.example.com"))
                        .buildHttpClient())
                .isNotNull();
        assertThat(CommonHttpClientBuilder.createBuilder(SSLContext.getDefault(), true)
                        .withProxy(URI.create("http://proxy.example.com"))
                        .buildHttpClient())
                .isNotNull();
        assertThat(builder().toString()).contains("CommonHttpClientBuilder").contains("connectTimeout=10000");
        assertThat(KeyStore.getDefaultType()).isNotBlank();
    }

    @Test
    @DisplayName("Интерцептор пишет запрос и ответ в DEBUG; со статусом, если он не 200")
    void loggingInterceptorLogsRequestAndResponse() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        Level previous = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        try {
            RestTemplate restTemplate = builder()
                    .withName("logged")
                    .withLoggingInterceptorExtensions(new LoggingInterceptorExtensions()
                            .logRequestInDebug(true)
                            .logResponseInDebug(true)
                            .logResponseStatus(true))
                    .buildRestTemplate();
            restTemplate.postForObject(baseUrl + "/echo", "payload", String.class);
            restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) {
                    return false;
                }
            });
            restTemplate.getForEntity(baseUrl + "/missing", String.class);

            assertThat(appender.list)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .anyMatch(m -> m.contains("[logged] RestTemplate request") && m.contains("payload"))
                    .anyMatch(m -> m.contains("RestTemplate response: echo:payload"))
                    .anyMatch(m -> m.contains("404") && m.contains("nope"));
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previous);
        }
    }

    @Test
    @DisplayName("Интерцептор в режиме TRACE и без DEBUG не меняет ответ")
    void loggingInterceptorOtherModes() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        Level previous = logger.getLevel();
        try {
            logger.setLevel(Level.TRACE);
            RestTemplate traced = builder()
                    .withLoggingInterceptorExtensions(new LoggingInterceptorExtensions())
                    .buildRestTemplate();
            assertThat(traced.getForObject(baseUrl + "/echo", String.class)).isEqualTo("echo:");

            logger.setLevel(Level.INFO);
            RestTemplate quiet = builder().buildRestTemplate();
            assertThat(quiet.getForObject(baseUrl + "/echo", String.class)).isEqualTo("echo:");
        } finally {
            logger.setLevel(previous);
        }
    }

    @Test
    @DisplayName("Обёртка ответа читает тело в память и отдаёт его повторно")
    void responseWrapperBuffersBody() throws Exception {
        MockClientHttpResponse source = new MockClientHttpResponse("body".getBytes(StandardCharsets.UTF_8), 503);
        LoggingHttpResponseWrapper wrapper = new LoggingHttpResponseWrapper(source);

        assertThat(wrapper.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(wrapper.getStatusText()).isNotNull();
        assertThat(wrapper.getHeaders()).isNotNull();
        assertThat(wrapper.getBodyBytes()).isEqualTo("body".getBytes(StandardCharsets.UTF_8));
        assertThat(wrapper.getBody().readAllBytes()).isEqualTo("body".getBytes(StandardCharsets.UTF_8));
        assertThat(wrapper.getBody().readAllBytes()).isEqualTo("body".getBytes(StandardCharsets.UTF_8));
        wrapper.close();
    }

    @Test
    @DisplayName("LostConnectionRetryHandler повторяет один раз и только при проблемах соединения")
    void lostConnectionRetry() {
        LostConnectionRetryHandler handler = new LostConnectionRetryHandler();
        BasicHttpRequest request = new BasicHttpRequest("GET", "/x");

        assertThat(handler.retryRequest(request, new NoHttpResponseException("x"), 1, null))
                .isTrue();
        assertThat(handler.retryRequest(request, new ConnectException("refused"), 1, null))
                .isTrue();
        assertThat(handler.retryRequest(request, new ConnectTimeoutException("t"), 1, null))
                .isTrue();
        assertThat(handler.retryRequest(request, new NoHttpResponseException("x"), 2, null))
                .isFalse();

        assertThat(handler.retryRequest(request, new SocketTimeoutException("Connect timed out"), 1, null))
                .isTrue();
        assertThat(handler.retryRequest(request, new SocketTimeoutException("Read timed out"), 1, null))
                .isFalse();
        assertThat(handler.retryRequest(request, new SocketTimeoutException(), 1, null))
                .isFalse();
        assertThat(handler.retryRequest(request, new IOException("other"), 1, null))
                .isFalse();

        assertThat(handler.retryRequest((org.apache.hc.core5.http.HttpResponse) null, 1, null))
                .isFalse();
        assertThat(handler.getRetryInterval(null, 1, null).toMilliseconds()).isZero();
    }

    @Test
    @DisplayName("InterruptedRequestRetryHandler не повторяет неизвестный хост, но повторяет таймаут чтения")
    void interruptedRequestRetry() {
        InterruptedRequestRetryHandler handler = new InterruptedRequestRetryHandler(3);
        BasicHttpRequest request = new BasicHttpRequest("GET", "/x");

        assertThat(handler.retryRequest(request, new SocketTimeoutException("Read timed out"), 1, null))
                .isTrue();
        assertThat(handler.retryRequest(request, new UnknownHostException("h"), 1, null))
                .isFalse();
        assertThat(handler.retryRequest(request, new SocketTimeoutException("Read timed out"), 4, null))
                .isFalse();
    }
}
