package ru.akvine.wild.bot.services.integration.property;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.property.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.property.dto.PropertyResponse;

@Service
@Slf4j
public class CustodianPropertyServiceIntegration {
    @Value("${custodian.url}")
    private String url;
    @Value("${custodian.method}")
    private String method;
    @Value("${custodian.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public PropertyResponse getProperties(GetPropertiesRequest request) {
        logger.info("Get properties by request = {}", request);

        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<GetPropertiesRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<PropertyResponse> response;
        try {
            response = restTemplate.postForEntity(
                    url + method,
                    httpEntity,
                    PropertyResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling custodian api method = [%s]. Message = [%s]",
                    transformMethod(method), exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return response.getBody();
    }

    private HttpHeaders buildHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private String transformMethod(String method) {
        return method.replace("/", "").toUpperCase();
    }
}
