package ru.akvine.wild.bot.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import ru.akvine.wild.bot.admin.dto.common.ErrorResponse;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService.BeginResult;

/**
 * Идемпотентность HTTP API по заголовку {@code Idempotency-Key}. Запрос с ключом выполняется один раз;
 * повторный запрос с тем же ключом получает сохранённый ответ (с заголовком {@code Idempotent-Replayed: true})
 * вместо повторного выполнения.
 * <ul>
 *     <li>ключ действует в пределах пользователя, метода и пути: чужой ключ ответ не вернёт;</li>
 *     <li>тот же ключ с другим телом или параметрами - 422 {@code IDEMPOTENCY_KEY_REUSED};</li>
 *     <li>тот же ключ, пока первый запрос выполняется, - 409 {@code IDEMPOTENCY_KEY_IN_PROGRESS};</li>
 *     <li>ответы 5xx не сохраняются, ключ освобождается: запрос можно повторить;</li>
 *     <li>запросы без заголовка, от неаутентифицированного пользователя и с телом больше лимита
 *     обрабатываются как обычно.</li>
 * </ul>
 * Должен стоять в цепочке Spring Security после проверки авторизации.
 */
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {
    static final String REPLAYED_HEADER = "Idempotent-Replayed";
    static final String IN_PROGRESS_CODE = "IDEMPOTENCY_KEY_IN_PROGRESS";
    static final String REUSED_CODE = "IDEMPOTENCY_KEY_REUSED";
    static final String INVALID_CODE = "IDEMPOTENCY_KEY_INVALID";
    private static final int MAX_KEY_LENGTH = 255;

    private final IdempotencyService idempotencyService;
    private final IdempotencyPayloadSerializer serializer;
    private final IdempotencyProperties.Http properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Duration inProgressTtl;
    private final Duration resultTtl;

    public IdempotencyFilter(
            IdempotencyService idempotencyService,
            IdempotencyPayloadSerializer serializer,
            IdempotencyProperties.Http properties) {
        this.idempotencyService = idempotencyService;
        this.serializer = serializer;
        this.properties = properties;
        this.inProgressTtl = Duration.ofSeconds(properties.getInProgressTtlSeconds());
        this.resultTtl = Duration.ofHours(properties.getResultTtlHours());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.getMethods().contains(request.getMethod())) {
            return true;
        }
        if (!StringUtils.hasText(request.getHeader(properties.getHeader()))) {
            return true;
        }
        String path = request.getServletPath();
        return properties.getPathPatterns().stream().noneMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String clientKey = request.getHeader(properties.getHeader()).trim();
        if (clientKey.isEmpty() || clientKey.length() > MAX_KEY_LENGTH) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, INVALID_CODE,
                    "Idempotency key must be 1.." + MAX_KEY_LENGTH + " characters");
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }
        if (request.getContentLengthLong() > properties.getMaxBodyBytes()) {
            logger.warn("Request body is larger than {} bytes, idempotency is skipped", properties.getMaxBodyBytes());
            chain.doFilter(request, response);
            return;
        }

        byte[] body = request.getInputStream().readAllBytes();
        String path = request.getServletPath();
        String storeKey = "http:" + Fingerprints.of(authentication.getName(), request.getMethod(), path, clientKey);
        String fingerprint = Fingerprints.of(body, request.getMethod(), path, request.getQueryString());

        BeginResult begin = idempotencyService.begin(storeKey, fingerprint, inProgressTtl);
        switch (begin.outcome()) {
            case REPLAY:
                replay(response, begin.payload());
                return;
            case IN_PROGRESS:
                writeError(response, HttpServletResponse.SC_CONFLICT, IN_PROGRESS_CODE,
                        "Request with this idempotency key is still being processed");
                return;
            case MISMATCH:
                writeError(response, 422, REUSED_CODE,
                        "Idempotency key was already used with a different request");
                return;
            default:
                break;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(new ReplayableRequestWrapper(request, body), responseWrapper);
        } catch (IOException | ServletException | RuntimeException e) {
            idempotencyService.release(storeKey);
            throw e;
        }

        try {
            if (responseWrapper.getStatus() >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                idempotencyService.release(storeKey);
            } else {
                StoredResponse stored = new StoredResponse(
                        responseWrapper.getStatus(),
                        responseWrapper.getContentType(),
                        Base64.getEncoder().encodeToString(responseWrapper.getContentAsByteArray()));
                idempotencyService.complete(storeKey, serializer.toJson(stored), resultTtl);
            }
        } catch (RuntimeException e) {
            logger.error("Can't save response for idempotency key", e);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    private void replay(HttpServletResponse response, String payload) throws IOException {
        StoredResponse stored = serializer.fromJson(payload, StoredResponse.class);
        response.setStatus(stored.status());
        if (stored.contentType() != null) {
            response.setContentType(stored.contentType());
        }
        response.setHeader(REPLAYED_HEADER, "true");
        response.getOutputStream().write(Base64.getDecoder().decode(stored.bodyBase64()));
    }

    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(new ErrorResponse(code, message)));
    }
}
