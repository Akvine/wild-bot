package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestLog extends HttpMessageLog {
    private String httpRequestMethod;
    private String requestURI;
    private String queryString;
    private String remoteAddr;
    private String sessionId;
    private String user;
}
