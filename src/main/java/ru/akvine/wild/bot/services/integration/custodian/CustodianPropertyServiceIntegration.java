package ru.akvine.wild.bot.services.integration.custodian;

import static ru.akvine.wild.bot.services.integration.custodian.CustodianPropertyServiceIntegration.CustodianApiMethods.GET_PROPERTIES;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.custodian.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.custodian.dto.PropertyResponse;

@Service
@Slf4j
@ConditionalOnProperty(name = "custodian.integration.enabled", havingValue = "true")
public class CustodianPropertyServiceIntegration implements CustodianIntegrationService {
    @Value("${custodian.url}")
    private String url;

    @Value("${custodian.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public PropertyResponse getProperties(GetPropertiesRequest request) {
        logger.info("Get properties by request = {}", request);

        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<GetPropertiesRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<PropertyResponse> response;
        try {
            response = restTemplate.postForEntity(url + GET_PROPERTIES, httpEntity, PropertyResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling custodian api method = [%s]. Message = [%s]",
                    transformMethod(GET_PROPERTIES.getUrl()), exception.getMessage());
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

    @Getter
    @AllArgsConstructor
    enum CustodianApiMethods {
        GET_PROPERTIES("/get", HttpMethod.POST);

        private final String url;
        private final HttpMethod method;
    }
}
