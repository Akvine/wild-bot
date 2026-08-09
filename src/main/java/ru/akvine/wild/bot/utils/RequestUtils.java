package ru.akvine.wild.bot.utils;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MapUtils;

@UtilityClass
public class RequestUtils {
    public String buildUri(String url, Map<String, String> queryParams) {
        if (MapUtils.isEmpty(queryParams)) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?");

        for (Map.Entry<String, String> paramWithValue : queryParams.entrySet()) {
            sb.append(paramWithValue.getKey())
                    .append("=")
                    .append(paramWithValue.getValue())
                    .append("&");
        }
        String urlWithParams = sb.toString();
        return urlWithParams.substring(0, urlWithParams.length() - 1);
    }
}
