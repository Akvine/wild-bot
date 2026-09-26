package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Форматирует запрос/ответ в текст. Значения заголовков из чёрного списка маскируются
 */
public class SimpleLogFormatter extends AbstractHttpMessageLogFormatter {

    private Set<String> blackListHeaders = new HashSet<>();
    private Set<String> whiteListHeaders = new HashSet<>();

    public SimpleLogFormatter(Set<String> headersBlackList, Set<String> headersWhiteList) {
        this.blackListHeaders = headersBlackList;
        this.whiteListHeaders = headersWhiteList;
    }

    public SimpleLogFormatter() {}

    @Override
    public String format(RequestLog requestLog) {
        StringBuilder msg = new StringBuilder();
        msg.append(requestLog.getHttpRequestMethod())
                .append(" ")
                .append(requestLog.getRequestURI())
                .append("\n");

        if (requestLog.getQueryString() != null) {
            msg.append('?').append(requestLog.getQueryString()).append("\n");
        }

        if (StringUtils.hasLength(requestLog.getRemoteAddr())) {
            msg.append("remoteAddr: ").append(requestLog.getRemoteAddr()).append("\n");
        }

        if (StringUtils.hasLength(requestLog.getSessionId())) {
            msg.append("sessionId: ").append(requestLog.getSessionId()).append("\n");
        }

        if (StringUtils.hasLength(requestLog.getUser())) {
            msg.append("user: ").append(requestLog.getUser()).append("\n");
        }

        appendMaskedHeaders(msg, requestLog);

        appendBody(msg, requestLog);

        return msg.toString();
    }

    @Override
    public String format(ResponseLog responseLog) {
        StringBuilder msg = new StringBuilder();
        msg.append("\nstatus: ").append(responseLog.getStatus()).append("\n");

        appendMaskedHeaders(msg, responseLog);

        appendBody(msg, responseLog);

        return msg.toString();
    }

    private void appendBody(StringBuilder msg, HttpMessageLog httpMessageLog) {
        String bodyAsString = httpMessageLog.bodyAsString();
        if (StringUtils.hasLength(bodyAsString)) {
            msg.append("body:\n").append(bodyAsString);
        }
    }

    private void appendMaskedHeaders(StringBuilder msg, HttpMessageLog httpMessageLog) {
        Map<String, List<String>> httpHeaders = httpMessageLog.getHttpHeaders();
        if (!CollectionUtils.isEmpty(httpHeaders)) {
            msg.append("headers: \n");
            httpHeaders.forEach((key, values) -> {
                msg.append(key).append(": ");
                String lowerKey = key.toLowerCase();
                boolean mask = !whiteListHeaders.contains(key)
                        && (blackListHeaders.stream().anyMatch(bl -> lowerKey.contains(bl.toLowerCase()))
                                || (lowerKey.contains("api") && lowerKey.contains("key")));
                msg.append(mask ? maskingValues(values) : values);
                msg.append("\n");
            });
        }
    }

    private List<String> maskingValues(List<String> values) {
        return values.stream()
                .map(val -> {
                    if (val.length() >= 5) {
                        StringBuilder maskedStr = new StringBuilder(val.substring(0, 1));
                        for (int i = 0; i < val.length() - 2; i++) {
                            maskedStr.append("*");
                        }
                        maskedStr.append(val.substring(val.length() - 1));
                        return maskedStr.toString();
                    } else {
                        return "****";
                    }
                })
                .collect(Collectors.toList());
    }
}
