package ru.akvine.wild.bot.telegram.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetWebhookResponse {
    private boolean isOk;
}
