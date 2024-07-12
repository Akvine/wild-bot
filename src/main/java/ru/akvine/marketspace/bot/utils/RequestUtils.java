package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

@UtilityClass
public class RequestUtils {
    public String buildUri(String url, Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?");

        List<Map.Entry<String, String>> entries = queryParams.entrySet().stream().toList();
        for (int i = 0; i < entries.size(); ++i) {
            sb.append(entries.get(i).getKey()).append("=").append(entries.get(i).getValue());
            if (i != entries.size() - 1) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
}
