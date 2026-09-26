package ru.akvine.wild.bot.infrastructure.httplogging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ServletRequestPathUtils;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.MethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.RequestLog;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.ResponseLog;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer.RequestLogWriter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer.ResponseLogWriter;

/**
 * Логирует HTTP-запросы и ответы. Тело запроса пишется в лог, как только приложение его вычитало
 * (если не вычитало - по завершении обработки), ответ - после обработки запроса. Тела обрезаются
 * до {@code maxPayloadLength} байт
 */
@Setter
@Slf4j
public class HttpLoggingFilter extends OncePerRequestFilter {
    private static final Set<String> CONTENT_ENCODINGS =
            new HashSet<>(Arrays.asList("gzip", "compress", "deflate", "br"));
    private static final byte[] ENCODED_PAYLOAD_DUMMY = "[encoded payload]".getBytes();
    private static final String CONTENT_ENCODING_HEADER = "Content-Encoding";

    private int maxPayloadLength = 4096;
    private boolean includeRemoteAddr = false;
    private boolean includeHeaders = true;
    private boolean includeQueryString = true;
    private boolean includePayload = true;
    private boolean unknownEncodedBodyToHex = false;
    private boolean forcePrintEncodedPayload = false;
    private List<String> requestBodyExcludePaths;
    private List<String> responseBodyExcludeContentTypes;
    private final MethodLoggingChecker methodLoggingChecker;
    private final RequestLogWriter requestLogWriter;
    private final ResponseLogWriter responseLogWriter;

    public HttpLoggingFilter(
            MethodLoggingChecker methodLoggingChecker,
            RequestLogWriter requestLogWriter,
            ResponseLogWriter responseLogWriter) {
        this.methodLoggingChecker = methodLoggingChecker;
        this.requestLogWriter = requestLogWriter;
        this.responseLogWriter = responseLogWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        initRequestPath(request);

        boolean shouldLogRequest = methodLoggingChecker.shouldLogRequest(request);
        boolean shouldLogResponse = methodLoggingChecker.shouldLogResponse(request);
        if (!shouldLogRequest && !shouldLogResponse) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpRequestLoggingContext requestContext = new HttpRequestLoggingContext();
        CachingRequestWrapper requestWrapper = new CachingRequestWrapper(request, maxPayloadLength, requestContext);
        CachingResponseWrapper responseWrapper = new CachingResponseWrapper(response, maxPayloadLength);

        if (shouldLogRequest) {
            logRequest(requestWrapper, requestContext);
        }

        try {
            filterChain.doFilter(requestWrapper, responseWrapper); // пускаем запрос дальше
        } finally {
            // тело могло быть не вычитано приложением - тогда лог запроса ещё не записан
            if (!requestContext.wasBodyLogged()) {
                try {
                    requestContext.readFinished();
                } catch (Exception e) {
                    logger.error("can't log request", e);
                }
            }
            if (shouldLogResponse) {
                logResponse(responseWrapper);
            }
        }
    }

    /**
     * {@code SpringMvcMethodLoggingChecker} сопоставляет запрос с методами контроллеров, а Spring MVC
     * для этого требует уже разобранный путь запроса, который до фильтров не кладётся
     */
    private void initRequestPath(HttpServletRequest request) {
        if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
            ServletRequestPathUtils.parseAndCache(request);
        }
    }

    private void logRequest(CachingRequestWrapper request, HttpRequestLoggingContext httpRequestLoggingContext) {
        try {
            RequestLog requestLog = new RequestLog();
            requestLog.setUnknownToHex(unknownEncodedBodyToHex);
            requestLog.setHttpRequestMethod(request.getMethod());
            requestLog.setRequestURI(request.getRequestURI());

            if (includeQueryString) {
                requestLog.setQueryString(request.getQueryString());
            }

            if (includeHeaders) {
                requestLog.setHttpHeaders(extractHeaders(request));
            }

            if (includeRemoteAddr) {
                requestLog.setRemoteAddr(request.getRemoteAddr());
            }

            if (isIncludePayload(request)) {
                httpRequestLoggingContext.subscribeForBody(bytes -> {
                    try {
                        requestLog.setEncoding(request.getCharacterEncoding());
                        requestLog.setBody(bytes);
                        requestLogWriter.log(requestLog);
                    } catch (Exception e) {
                        logger.error("can't log request", e);
                    }
                });
                if (!hasBody(request)) {
                    httpRequestLoggingContext.readFinished();
                }
            } else {
                requestLogWriter.log(requestLog);
            }
        } catch (Exception e) {
            logger.error("can't log request", e);
        }
    }

    private boolean hasBody(HttpServletRequest request) {
        return request.getContentLengthLong() > 0 || request.getHeader("Transfer-Encoding") != null;
    }

    private void logResponse(CachingResponseWrapper response) {
        try {
            response.flushCache();

            ResponseLog responseLog = new ResponseLog();
            responseLog.setUnknownToHex(unknownEncodedBodyToHex);
            responseLog.setStatus(response.getStatus());
            responseLog.setEncoding(response.getCharacterEncoding());
            responseLog.setHttpHeaders(getHeaders(response));
            if (!forcePrintEncodedPayload && isBodyEncoded(response)) {
                responseLog.setBody(ENCODED_PAYLOAD_DUMMY);
            } else if (!isExcludeContentType(response)) {
                responseLog.setBody(response.cachedBody());
            } else {
                responseLog.setBody("Body logging is disabled for this content-type".getBytes());
            }

            responseLogWriter.log(responseLog);
        } catch (Exception e) {
            logger.error("can't log response", e);
        }
    }

    private Map<String, List<String>> extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> headerMap = new HashMap<>();
        for (String headerName : Collections.list(request.getHeaderNames())) {
            headerMap.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return headerMap;
    }

    private Map<String, List<String>> getHeaders(HttpServletResponse response) {
        Map<String, List<String>> headersMap = new HashMap<>();
        response.getHeaderNames().forEach(name -> {
            List<String> values = headersMap.computeIfAbsent(name, s -> new ArrayList<>());
            values.addAll(response.getHeaders(name));
        });
        return headersMap;
    }

    private boolean isBodyEncoded(HttpServletResponse response) {
        return response.getHeaders(CONTENT_ENCODING_HEADER).stream().anyMatch(CONTENT_ENCODINGS::contains);
    }

    private boolean isIncludePayload(HttpServletRequest request) {
        return includePayload && !isExcludePath(request);
    }

    private boolean isExcludePath(HttpServletRequest request) {
        if (requestBodyExcludePaths != null && !requestBodyExcludePaths.isEmpty()) {
            String servletPath = request.getServletPath();
            for (String excludePath : requestBodyExcludePaths) {
                if (servletPath.contains(excludePath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExcludeContentType(HttpServletResponse response) {
        if (responseBodyExcludeContentTypes != null && !responseBodyExcludeContentTypes.isEmpty()) {
            return responseBodyExcludeContentTypes.contains(response.getContentType());
        }
        return false;
    }
}
