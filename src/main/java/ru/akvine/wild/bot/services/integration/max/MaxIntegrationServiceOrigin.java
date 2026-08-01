package ru.akvine.wild.bot.services.integration.max;

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
import ru.akvine.wild.bot.services.integration.max.dto.LongPoolingSubscriptionResponse;
import ru.akvine.wild.bot.services.integration.max.dto.Update;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Service
public class MaxIntegrationServiceOrigin implements MaxIntegrationService {
    @Value("${max.bot.url}")
    private String maxUrl;
    @Value("${max.bot.token}")
    private String maxBotToken;

    private final RestTemplate restTemplate;

    // TODO: поменять на инжект RestTemplate и перейти к @RequiredArgsConstructor из Lombok
    public MaxIntegrationServiceOrigin() {
        try {
            this.restTemplate = buildRestTemplate();
        } catch (Exception exception) {
            throw new RuntimeException("Exception while initialize " +
                    MaxIntegrationServiceOrigin.class.getSimpleName() +
                    " class. Ex = " + exception);
        }
    }

    @Override
    public Update[] updates() {
        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<LongPoolingSubscriptionResponse> response;
        try {
            response = restTemplate.exchange(maxUrl + MaxApiMethods.LONG_POOLING_SUBSCRIPTIONS_GET.getMethod(),
                    HttpMethod.GET,
                    httpEntity,
                    LongPoolingSubscriptionResponse.class);

            if (response.getBody() == null) {
                return new Update[0];
            }

            return response.getBody().getUpdates();
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling max api method = [%s]. Message = %s",
                    MaxApiMethods.LONG_POOLING_SUBSCRIPTIONS_GET, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }


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
    private RestTemplate buildRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
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

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    @AllArgsConstructor
    @Getter
    enum MaxApiMethods {
        LONG_POOLING_SUBSCRIPTIONS_GET("/updates");

        private final String method;
    }
}
