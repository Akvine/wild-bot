package ru.akvine.wild.bot.infrastructure.httplogging;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.event.Level;

/**
 * Настройки логирования HTTP-запросов и ответов (префикс {@code http.logging})
 */
@Getter
@Setter
public class HttpLoggingProperties {
    private int maxPayloadLength = 4096;
    private boolean unknownEncodedBodyToHex = false;
    private boolean includeRemoteAddr = false;
    private boolean includeHeaders = true;
    private boolean includeQueryString = true;
    private boolean includePayload = true;
    private boolean enabled = false;
    private boolean ignoreSwagger = false;
    private boolean ignoreActuator = false;
    private boolean forcePrintEncodedPayload = false;
    private String loggerName = "ru.akvine.wild.bot.httplogging";
    private Level level = Level.INFO;
    private List<String> requestBodyExcludePaths;
    private List<String> responseBodyExcludeContentTypes;
    private Set<String> headersBlackList = new HashSet<>();
    private Set<String> headersWhiteList = new HashSet<>();
}
