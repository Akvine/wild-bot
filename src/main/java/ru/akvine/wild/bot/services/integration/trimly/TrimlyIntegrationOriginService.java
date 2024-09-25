package ru.akvine.wild.bot.services.integration.trimly;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.trimly.dto.ShortUrlRequest;
import ru.akvine.wild.bot.services.integration.trimly.dto.ShortUrlResponse;

@Service
@Slf4j
public class TrimlyIntegrationOriginService implements TrimlyIntegrationService {
    @Value("${trimly.url}")
    private String url;
    @Value("${trimly.method}")
    private String method;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ShortUrlResponse createTempShortUrl(String originUrl) {
        logger.info("Generate short link in Trimly");

        ShortUrlRequest request = new ShortUrlRequest(originUrl);
        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<ShortUrlRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<ShortUrlResponse> response;
        try {
            response = restTemplate.postForEntity(
                    url + method,
                    httpEntity,
                    ShortUrlResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling Trimly api method = [%s]. Message = [%s]",
                    transformMethod(method), exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return response.getBody();
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String transformMethod(String method) {
        return method.replace("/", "").toUpperCase();
    }
}
