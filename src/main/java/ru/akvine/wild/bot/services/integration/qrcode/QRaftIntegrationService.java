package ru.akvine.wild.bot.services.integration.qrcode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;

@Service
@RequiredArgsConstructor
@Slf4j
public class QRaftIntegrationService implements QrCodeGenerationService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${qraft.generate.qrcode.url}")
    private String url;
    @Value("${qraft.generate.qrcode.method}")
    private String method;

    @Override
    public byte[] generateQrCode(GenerateQrCodeRequest request) {
        logger.info("Generate qr code in QRaft service by request = {}", request);

        HttpHeaders headers = buildHttpHeaders();
        HttpEntity<GenerateQrCodeRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.postForEntity(
                    url + method,
                    httpEntity,
                    byte[].class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling QRaft api method = [%s]. Message = [%s]",
                    transformMethod(method), exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return response.getBody();
    }

    @Override
    public QrCodeGenerationServiceType getType() {
        return QrCodeGenerationServiceType.EXTERNAL;
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
