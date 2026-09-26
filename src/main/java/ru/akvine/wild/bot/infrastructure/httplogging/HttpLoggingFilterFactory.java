package ru.akvine.wild.bot.infrastructure.httplogging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.ActuatorLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.AllowAllMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.CompositeMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.MethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.SpringMvcMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.SwaggerLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.SimpleLogFormatter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer.Slf4jHttpLogWriter;

/**
 * Собирает {@link HttpLoggingFilter} из {@link HttpLoggingProperties}
 */
public final class HttpLoggingFilterFactory {
    private static final Set<String> DEFAULT_HEADERS_BLACK_LIST =
            Set.of("Authentication", "Authorization", "Cookie", "secret", "password");

    private HttpLoggingFilterFactory() {}

    public static HttpLoggingFilter create(
            HttpLoggingProperties properties, ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
        Set<String> blackList = new HashSet<>(DEFAULT_HEADERS_BLACK_LIST);
        blackList.addAll(properties.getHeadersBlackList());
        SimpleLogFormatter formatter = new SimpleLogFormatter(blackList, properties.getHeadersWhiteList());
        Slf4jHttpLogWriter writer =
                new Slf4jHttpLogWriter(formatter, formatter, properties.getLoggerName(), properties.getLevel());

        HttpLoggingFilter filter = new HttpLoggingFilter(checker(properties, handlerMappings), writer, writer);
        filter.setMaxPayloadLength(properties.getMaxPayloadLength());
        filter.setIncludeHeaders(properties.isIncludeHeaders());
        filter.setIncludePayload(properties.isIncludePayload());
        filter.setIncludeQueryString(properties.isIncludeQueryString());
        filter.setIncludeRemoteAddr(properties.isIncludeRemoteAddr());
        filter.setUnknownEncodedBodyToHex(properties.isUnknownEncodedBodyToHex());
        filter.setForcePrintEncodedPayload(properties.isForcePrintEncodedPayload());
        filter.setRequestBodyExcludePaths(properties.getRequestBodyExcludePaths());
        filter.setResponseBodyExcludeContentTypes(properties.getResponseBodyExcludeContentTypes());
        return filter;
    }

    private static MethodLoggingChecker checker(
            HttpLoggingProperties properties, ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
        List<MethodLoggingChecker> checkers = new ArrayList<>();
        checkers.add(new AllowAllMethodLoggingChecker());
        checkers.add(new SpringMvcMethodLoggingChecker(handlerMappings));
        if (properties.isIgnoreActuator()) {
            checkers.add(new ActuatorLoggingChecker());
        }
        if (properties.isIgnoreSwagger()) {
            checkers.add(new SwaggerLoggingChecker());
        }
        return new CompositeMethodLoggingChecker(checkers);
    }
}
