package ru.akvine.wild.bot.unit.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyFilter;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyPayloadSerializer;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyProperties;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService;
import ru.akvine.wild.bot.infrastructure.idempotency.InMemoryIdempotencyStore;

class IdempotencyFilterTest {
    private static final String PATH = "/admin/clients/send/message";

    private final IdempotencyPayloadSerializer serializer = new IdempotencyPayloadSerializer();
    private final IdempotencyFilter filter = new IdempotencyFilter(
            new IdempotencyService(new InMemoryIdempotencyStore(), serializer),
            serializer,
            new IdempotencyProperties().getHttp());
    private final AtomicInteger handled = new AtomicInteger();

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin", "n/a", List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest request(String key, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        request.setServletPath(PATH);
        if (key != null) {
            request.addHeader("Idempotency-Key", key);
        }
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    /** Контроллер: эхо тела запроса со статусом 201; считает, сколько раз его вызвали */
    private FilterChain controller(int status) {
        return (req, res) -> {
            handled.incrementAndGet();
            String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            HttpServletResponse response = (HttpServletResponse) res;
            response.setStatus(status);
            response.setContentType("application/json");
            response.getOutputStream().write(("{\"echo\":\"" + body + "\"}").getBytes(StandardCharsets.UTF_8));
        };
    }

    private MockHttpServletResponse call(MockHttpServletRequest request, FilterChain chain) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    @DisplayName("Повторный запрос с тем же ключом получает сохранённый ответ и не доходит до контроллера")
    void repeatedRequestIsReplayed() throws Exception {
        MockHttpServletResponse first = call(request("key-1", "hello"), controller(201));
        MockHttpServletResponse second = call(request("key-1", "hello"), controller(201));

        assertThat(handled).hasValue(1);
        assertThat(first.getStatus()).isEqualTo(201);
        assertThat(first.getContentAsString()).isEqualTo("{\"echo\":\"hello\"}");
        assertThat(first.getHeader("Idempotent-Replayed")).isNull();
        assertThat(second.getStatus()).isEqualTo(201);
        assertThat(second.getContentAsString()).isEqualTo("{\"echo\":\"hello\"}");
        assertThat(second.getContentType()).startsWith("application/json");
        assertThat(second.getHeader("Idempotent-Replayed")).isEqualTo("true");
    }

    @Test
    @DisplayName("Тот же ключ с другим телом - 422, контроллер не вызывается")
    void sameKeyWithDifferentBodyIsRejected() throws Exception {
        call(request("key-1", "hello"), controller(201));

        MockHttpServletResponse second = call(request("key-1", "other"), controller(201));

        assertThat(handled).hasValue(1);
        assertThat(second.getStatus()).isEqualTo(422);
        assertThat(second.getContentAsString()).contains("IDEMPOTENCY_KEY_REUSED");
    }

    @Test
    @DisplayName("Пока первый запрос выполняется, повтор получает 409")
    void requestDuringExecutionGetsConflict() throws Exception {
        MockHttpServletResponse[] nested = new MockHttpServletResponse[1];
        FilterChain slowController = (req, res) -> {
            nested[0] = call(request("key-1", "hello"), controller(201));
            ((HttpServletResponse) res).setStatus(201);
        };

        call(request("key-1", "hello"), slowController);

        assertThat(nested[0].getStatus()).isEqualTo(409);
        assertThat(nested[0].getContentAsString()).contains("IDEMPOTENCY_KEY_IN_PROGRESS");
        assertThat(handled).hasValue(0);
    }

    @Test
    @DisplayName("Ответ 5xx не сохраняется: тот же запрос выполняется заново")
    void serverErrorIsNotSaved() throws Exception {
        call(request("key-1", "hello"), controller(500));
        MockHttpServletResponse second = call(request("key-1", "hello"), controller(201));

        assertThat(handled).hasValue(2);
        assertThat(second.getStatus()).isEqualTo(201);
    }

    @Test
    @DisplayName("Исключение из цепочки освобождает ключ")
    void exceptionReleasesKey() throws Exception {
        FilterChain failing = (req, res) -> {
            throw new IllegalStateException("boom");
        };
        try {
            call(request("key-1", "hello"), failing);
        } catch (IllegalStateException expected) {
            // проброшено дальше, как и без фильтра
        }

        call(request("key-1", "hello"), controller(201));

        assertThat(handled).hasValue(1);
    }

    @Test
    @DisplayName("Ключ действует в пределах пользователя: чужой ключ ответ не возвращает")
    void keyIsScopedByUser() throws Exception {
        call(request("key-1", "hello"), controller(201));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("another-admin", "n/a", List.of()));

        call(request("key-1", "hello"), controller(201));

        assertThat(handled).hasValue(2);
    }

    @Test
    @DisplayName("Запросы без ключа, не из областей действия и от анонимного пользователя идут как обычно")
    void otherRequestsPassThrough() throws Exception {
        call(request(null, "hello"), controller(201));
        call(request(null, "hello"), controller(201));
        assertThat(handled).hasValue(2);

        MockHttpServletRequest getRequest = new MockHttpServletRequest("GET", PATH);
        getRequest.setServletPath(PATH);
        getRequest.addHeader("Idempotency-Key", "key-1");
        call(getRequest, controller(200));
        call(getRequest, controller(200));
        assertThat(handled).hasValue(4);

        MockHttpServletRequest otherPath = request("key-2", "hello");
        otherPath.setServletPath("/security/two/factor/auth/start");
        call(otherPath, controller(201));
        call(otherPath, controller(201));
        assertThat(handled).hasValue(6);

        SecurityContextHolder.getContext()
                .setAuthentication(new AnonymousAuthenticationToken(
                        "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        call(request("key-3", "hello"), controller(201));
        call(request("key-3", "hello"), controller(201));
        assertThat(handled).hasValue(8);
    }

    @Test
    @DisplayName("Слишком длинный ключ отклоняется с 400")
    void tooLongKeyIsRejected() throws Exception {
        MockHttpServletResponse response = call(request("k".repeat(256), "hello"), controller(201));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(handled).hasValue(0);
    }
}
