package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class RequestUtils {
    public String buildUri(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?");

        for (Map.Entry<String, String> paramWithValue : queryParams.entrySet()) {
            sb.append(paramWithValue.getKey()).append("=").append(paramWithValue.getValue()).append("&");
        }
        String urlWithParams = sb.toString();
        return urlWithParams.substring(0, urlWithParams.length() - 1);
    }
}
