package ru.akvine.marketspace.bot.telegram.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetWebhookInfoResponse {

    private boolean isOk;
    private WebhookInfo result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookInfo {
        private String url;
        private boolean hasCustomCertificate;
    }
}
