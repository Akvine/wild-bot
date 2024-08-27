package ru.akvine.wild.bot.telegram.webhook.dto;

import java.util.List;

public class SetWebhookRequest {
    private static final String TELEGRAM_REQUEST_METHOD = "/setWebhook?url=";
    private static final String ALLOWED_UPDATES_PARAM = "&allowed_updates=";

    private String telegramRequestMethod;

    public SetWebhookRequest(String telegramApiUrl, String botToken, String botPath) {
        this.telegramRequestMethod = telegramApiUrl + botToken + TELEGRAM_REQUEST_METHOD + botPath;
    }

    public void setAllowedUpdates(List<String> allowedUpdates) {
        StringBuilder sb = new StringBuilder();
        sb.append(ALLOWED_UPDATES_PARAM);
        sb.append("[");
        allowedUpdates.forEach((update) -> {
            sb.append("\"");
            sb.append(update);
            sb.append("\"");
            sb.append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");

        this.telegramRequestMethod = this.telegramRequestMethod + sb;
    }

    public String getTelegramRequestMethod() {
        return telegramRequestMethod;
    }
}
