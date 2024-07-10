package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class RequestUtils {
    public String buildUri(String url, Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("?");
        queryParams.keySet().forEach(param -> sb.append(param).append("=").append(queryParams.get(param)));
        return sb.toString();
    }
}
