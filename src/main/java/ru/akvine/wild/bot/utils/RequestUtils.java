package ru.akvine.wild.bot.utils;

import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MapUtils;

/**
 * Сборка URL с query-параметрами для исходящих HTTP-запросов к внешним интеграциям.
 */
@UtilityClass
public class RequestUtils {
    /**
     * Добавляет к URL query-параметры вида {@code ?key1=value1&key2=value2}.
     *
     * @param url         базовый URL без query-параметров
     * @param queryParams query-параметры; если пусты или {@code null} — URL возвращается без изменений
     * @return URL с добавленными query-параметрами
     */
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
