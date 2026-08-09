package ru.akvine.wild.bot.services.integration.max;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.net.ssl.SSLContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.GetMessagesRequest;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.max.dto.response.GetMessagesResponse;
import ru.akvine.wild.bot.services.integration.max.dto.response.LongPoolingSubscriptionResponse;
import ru.akvine.wild.bot.utils.RequestUtils;

@Service
public class MaxIntegrationServiceOrigin implements MaxIntegrationService {
    @Value("${max.bot.url}")
    private String maxUrl;

    @Value("${max.bot.token}")
    private String maxBotToken;

    @Value("${max.bot.dev.mode.long.pooling.timeout.seconds}")
    private String poolingTimeoutSeconds;

    @Value("${max.bot.dev.mode.long.pooling.update-types}")
    private String updateTypes;

    private final RestTemplate restTemplate;

    // TODO: поменять на инжект RestTemplate и перейти к @RequiredArgsConstructor из Lombok
    public MaxIntegrationServiceOrigin() {
        try {
            this.restTemplate = buildRestTemplate();
        } catch (Exception exception) {
            throw new RuntimeException("Exception while initialize " + MaxIntegrationServiceOrigin.class.getSimpleName()
                    + " class. Ex = "
                    + exception);
        }
    }

    @Override
    public Update[] updates() {
        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<LongPoolingSubscriptionResponse> response;
        String url = RequestUtils.buildUri(
                maxUrl + MaxApiMethods.LONG_POOLING_SUBSCRIPTIONS_GET.getEndpoint(),
                Map.of("timeout", poolingTimeoutSeconds, "types", updateTypes));
        try {
            response = restTemplate.exchange(
                    url,
                    MaxApiMethods.LONG_POOLING_SUBSCRIPTIONS_GET.getMethod(),
                    httpEntity,
                    LongPoolingSubscriptionResponse.class);

            if (response.getBody() == null) {
                return new Update[0];
            }

            return response.getBody().getUpdates();
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling MAX api method = [%s]. Message = %s",
                    MaxApiMethods.LONG_POOLING_SUBSCRIPTIONS_GET, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public Message[] getMessages(String chatId) {
        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<GetMessagesRequest> httpEntity = new HttpEntity<>(headers);

        String url =
                RequestUtils.buildUri(maxUrl + MaxApiMethods.GET_MESSAGES.getEndpoint(), Map.of("chat_id", chatId));
        ResponseEntity<GetMessagesResponse> response;
        try {
            response = restTemplate.exchange(
                    url, MaxApiMethods.GET_MESSAGES.getMethod(), httpEntity, GetMessagesResponse.class);

            if (response.getBody() == null) {
                return new Message[0];
            }

            return response.getBody().getMessages();
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling MAX api method = [%s]. Message = %s",
                    MaxApiMethods.GET_MESSAGES, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void sendMessage(String chatId, SendMessageRequest request) {
        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<SendMessageRequest> httpEntity = new HttpEntity<>(request, headers);

        String url =
                RequestUtils.buildUri(maxUrl + MaxApiMethods.SEND_MESSAGE.getEndpoint(), Map.of("chat_id", chatId));
        try {
            restTemplate.exchange(url, MaxApiMethods.SEND_MESSAGE.getMethod(), httpEntity, GetMessagesResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling MAX api method = [%s]. Message = %s",
                    MaxApiMethods.SEND_MESSAGE, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void downloadPhoto() {}

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, maxBotToken);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    /**
     * Собирает RestTemplate поверх HttpClient5 с доверием к любому TLS-сертификату сервера —
     * без этого запросы к {@code max.bot.url} падают с PKIX path building failed, если сертификат
     * сервера не подписан центром из системного trust store JVM.
     */
    private RestTemplate buildRestTemplate()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true)
                .build();

        SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();

        CloseableHttpClient httpClient =
                HttpClients.custom().setConnectionManager(connectionManager).build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    @AllArgsConstructor
    @Getter
    enum MaxApiMethods {
        LONG_POOLING_SUBSCRIPTIONS_GET("/updates", HttpMethod.GET),

        GET_MESSAGES("/messages", HttpMethod.GET),
        SEND_MESSAGE("/messages", HttpMethod.POST);

        private final String endpoint;
        private final HttpMethod method;
    }
}
