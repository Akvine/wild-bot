package ru.akvine.wild.bot.services.integration.max;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.GetMessagesRequest;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.max.dto.response.*;
import ru.akvine.wild.bot.utils.ByteUtils;
import ru.akvine.wild.bot.utils.RequestUtils;

@Service
public class MaxIntegrationServiceOrigin implements MaxIntegrationService {
    @Value("${max.bot.url}")
    private String maxUrl;

    @Value("${max.bot.token}")
    private String maxBotToken;

    @Value("${max.bot.long.pooling.timeout.seconds}")
    private String poolingTimeoutSeconds;

    @Value("${max.bot.long.pooling.update-types}")
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
    public byte[] downloadAttachment(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            return ByteUtils.convertToBytes(connection.getInputStream());
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while downloading MAX attachment from url = [%s]. Message = %s",
                    fileUrl, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public String getUploadFileUrl(AttachmentType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, maxBotToken);
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.add(HttpHeaders.ACCEPT, "*/*");
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        String url = RequestUtils.buildUri(
                maxUrl + MaxApiMethods.GET_UPLOAD_FILE_URL.getEndpoint(),
                Map.of("type", type.toString().toLowerCase()));

        ResponseEntity<GetUploadFileUrlResponse> response;
        try {
            response = restTemplate.exchange(
                    url, MaxApiMethods.GET_UPLOAD_FILE_URL.getMethod(), httpEntity, GetUploadFileUrlResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling MAX api method = [%s]. Message = %s",
                    MaxApiMethods.GET_UPLOAD_FILE_URL, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return response.getBody().getUrl();
    }

    @Override
    public String uploadFileAtServer(String url, byte[] file, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", new MultipartByteArrayResource(file, filename));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<UploadFileAtServerResponse> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, UploadFileAtServerResponse.class);
        } catch (Exception exception) {
            String errorMessage =
                    String.format("Error while uploading file at server for MAX. Message = %s", exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return response.getBody().getToken();
    }

    @Override
    public String uploadImageAtServer(String url, byte[] image, String imageName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", new MultipartByteArrayResource(image, imageName));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        URI uri = URI.create(url);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while uploading image at server for MAX. Message = %s", exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        String responseBody = response.getBody();
        try {
            JsonNode root = new ObjectMapper().readTree(responseBody);

            if (root.has("error_code")) {
                String errorCode = root.get("error_code").asText();
                String errorData =
                        root.has("error_data") ? root.get("error_data").asText() : "";
                throw new IntegrationException(
                        String.format("Server returned error: code=%s, data=%s", errorCode, errorData));
            }

            JsonNode photosNode = root.get("photos");
            if (photosNode == null || !photosNode.isObject()) {
                throw new IntegrationException("Response does not contain 'photos' field");
            }

            Iterator<Map.Entry<String, JsonNode>> fields = photosNode.fields();
            if (!fields.hasNext()) {
                throw new IntegrationException("No entries in 'photos' object");
            }

            Map.Entry<String, JsonNode> firstPhotoEntry = fields.next();
            JsonNode tokenNode = firstPhotoEntry.getValue().get("token");
            if (tokenNode == null) {
                throw new IntegrationException("Response does not contain 'token' field inside photo entry");
            }

            return tokenNode.asText();
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception exception) {
            throw new IntegrationException(
                    "Bad response while uploading image at server for MAX. Message = " + exception.getMessage());
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
        SEND_MESSAGE("/messages", HttpMethod.POST),
        GET_UPLOAD_FILE_URL("/uploads", HttpMethod.POST);

        private final String endpoint;
        private final HttpMethod method;
    }
}
