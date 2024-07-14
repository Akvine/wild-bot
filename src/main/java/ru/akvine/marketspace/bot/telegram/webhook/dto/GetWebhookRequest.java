package ru.akvine.marketspace.bot.telegram.webhook.dto;

public class GetWebhookRequest {

    private static final String TELEGRAM_REQUEST_METHOD = "/getWebhookInfo";

    private String telegramRequestMethod;

    public GetWebhookRequest(String telegramApiUrl, String botToken) {
        this.telegramRequestMethod = telegramApiUrl + botToken + TELEGRAM_REQUEST_METHOD;
    }

    public String getTelegramRequestMethod() {
        return telegramRequestMethod;
    }
}