package ru.akvine.wild.bot.infrastructure.httplogging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.AllowAllMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.MethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.RequestLog;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.ResponseLog;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer.RequestLogWriter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer.ResponseLogWriter;

@DisplayName("HttpLoggingFilter")
class HttpLoggingFilterTest {
    private final List<RequestLog> requests = new ArrayList<>();
    private final List<ResponseLog> responses = new ArrayList<>();
    private HttpLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = newFilter(new AllowAllMethodLoggingChecker());
    }

    private HttpLoggingFilter newFilter(MethodLoggingChecker checker) {
        RequestLogWriter requestWriter = requests::add;
        ResponseLogWriter responseWriter = responses::add;
        return new HttpLoggingFilter(checker, requestWriter, responseWriter);
    }

    private static MockHttpServletRequest post(String uri, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setServletPath(uri);
        request.setCharacterEncoding("UTF-8");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private static String text(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Тело запроса, прочитанное приложением через InputStream, попадает в лог вместе с заголовками и query")
    void logsRequestBodyReadByApplication() throws Exception {
        MockHttpServletRequest request = post("/orders", "{\"a\":1}");
        request.setQueryString("x=1");
        request.addHeader("X-Trace", "abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            ServletInputStream in = req.getInputStream();
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("{\"a\":1}");
        });

        assertThat(requests).hasSize(1);
        RequestLog log = requests.get(0);
        assertThat(log.getHttpRequestMethod()).isEqualTo("POST");
        assertThat(log.getRequestURI()).isEqualTo("/orders");
        assertThat(log.getQueryString()).isEqualTo("x=1");
        assertThat(log.getHttpHeaders()).containsKey("X-Trace");
        assertThat(text(log.getBody())).isEqualTo("{\"a\":1}");
    }

    @Test
    @DisplayName("Тело запроса, прочитанное через Reader, тоже логируется")
    void logsRequestBodyReadThroughReader() throws Exception {
        MockHttpServletRequest request = post("/orders", "line-1\nline-2");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertThat(req.getReader().lines()).containsExactly("line-1", "line-2");
        });

        assertThat(text(requests.get(0).getBody())).isEqualTo("line-1\nline-2");
    }

    @Test
    @DisplayName("Побайтовое чтение тела тоже попадает в кэш")
    void logsRequestBodyReadByteByByte() throws Exception {
        MockHttpServletRequest request = post("/orders", "abc");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            ServletInputStream in = req.getInputStream();
            int b;
            while ((b = in.read()) != -1) {
                assertThat(b).isPositive();
            }
        });

        assertThat(text(requests.get(0).getBody())).isEqualTo("abc");
    }

    @Test
    @DisplayName("Приложение не читало тело: запрос всё равно логируется после обработки")
    void logsRequestEvenIfBodyWasNotRead() throws Exception {
        MockHttpServletRequest request = post("/orders", "unread");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getBody()).isEmpty();
    }

    @Test
    @DisplayName("Запрос без тела логируется сразу")
    void logsRequestWithoutBodyImmediately() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ping");
        request.setServletPath("/ping");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getHttpRequestMethod()).isEqualTo("GET");
    }

    @Test
    @DisplayName("Тело обрезается до maxPayloadLength")
    void truncatesBody() throws Exception {
        filter.setMaxPayloadLength(5);
        MockHttpServletRequest request = post("/orders", "0123456789");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            req.getInputStream().readAllBytes();
            res.getOutputStream().write("abcdefghij".getBytes(StandardCharsets.UTF_8));
        });

        assertThat(text(requests.get(0).getBody())).isEqualTo("01234");
        assertThat(text(responses.get(0).getBody())).isEqualTo("abcde");
        assertThat(response.getContentAsString()).isEqualTo("abcdefghij");
    }

    @Test
    @DisplayName("Ответ, записанный через OutputStream, логируется и не искажается")
    void logsResponseFromOutputStream() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/data");
        request.setServletPath("/data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCharacterEncoding("UTF-8");

        filter.doFilter(request, response, (req, res) -> {
            ((HttpServletResponse) res).setStatus(201);
            ((HttpServletResponse) res).setHeader("X-Result", "ok");
            res.getOutputStream().write("ответ".getBytes(StandardCharsets.UTF_8));
            res.getOutputStream().flush();
        });

        ResponseLog log = responses.get(0);
        assertThat(log.getStatus()).isEqualTo(201);
        assertThat(log.getHttpHeaders()).containsKey("X-Result");
        assertThat(text(log.getBody())).isEqualTo("ответ");
        assertThat(response.getContentAsString(StandardCharsets.UTF_8)).isEqualTo("ответ");
    }

    @Test
    @DisplayName("Ответ, записанный через Writer, логируется и не искажается")
    void logsResponseFromWriter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/data");
        request.setServletPath("/data");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCharacterEncoding("UTF-8");

        filter.doFilter(request, response, (req, res) -> {
            res.getWriter().write("hello writer");
            res.getWriter().flush();
        });

        assertThat(text(responses.get(0).getBody())).isEqualTo("hello writer");
        assertThat(response.getContentAsString()).isEqualTo("hello writer");
    }

    @Test
    @DisplayName("Нельзя получить и InputStream, и Reader (OutputStream, и Writer) одновременно")
    void rejectsMixingStreamsAndReaders() throws Exception {
        MockHttpServletRequest request = post("/orders", "x");
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<Throwable> errors = new ArrayList<>();

        filter.doFilter(request, response, (req, res) -> {
            req.getInputStream();
            errors.add(catchThrowable(req::getReader));
            res.getWriter();
            errors.add(catchThrowable(res::getOutputStream));
        });
        MockHttpServletRequest other = post("/orders", "x");
        filter.doFilter(other, new MockHttpServletResponse(), (req, res) -> {
            req.getReader();
            errors.add(catchThrowable(req::getInputStream));
            res.getOutputStream();
            errors.add(catchThrowable(res::getWriter));
        });

        assertThat(errors).hasSize(4).allSatisfy(e -> assertThat(e).isInstanceOf(IllegalStateException.class));
    }

    private static Throwable catchThrowable(ThrowingRunnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    @Test
    @DisplayName("reset и resetBuffer ответа сбрасывают и кэш")
    void resetClearsCachedBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/data");
        request.setServletPath("/data");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            res.getOutputStream().write("first".getBytes(StandardCharsets.UTF_8));
            res.resetBuffer();
            res.getOutputStream().write("second".getBytes(StandardCharsets.UTF_8));
        });
        assertThat(text(responses.get(0).getBody())).isEqualTo("second");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            res.getOutputStream().write("third".getBytes(StandardCharsets.UTF_8));
            res.reset();
        });
        assertThat(responses.get(1).getBody()).isEmpty();
    }

    @Test
    @DisplayName("Исключённый путь: запрос логируется без тела")
    void excludedPathSkipsBody() throws Exception {
        filter.setRequestBodyExcludePaths(List.of("/secret"));
        MockHttpServletRequest request = post("/secret/upload", "password=1");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> req.getInputStream()
                .readAllBytes());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getBody()).isNull();
    }

    @Test
    @DisplayName("includePayload=false, includeHeaders=false, includeQueryString=false, includeRemoteAddr=true")
    void respectsIncludeFlags() throws Exception {
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setIncludeQueryString(false);
        filter.setIncludeRemoteAddr(true);
        MockHttpServletRequest request = post("/orders", "{}");
        request.setQueryString("q=1");
        request.addHeader("X-Trace", "abc");
        request.setRemoteAddr("10.1.2.3");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

        RequestLog log = requests.get(0);
        assertThat(log.getBody()).isNull();
        assertThat(log.getHttpHeaders()).isNull();
        assertThat(log.getQueryString()).isNull();
        assertThat(log.getRemoteAddr()).isEqualTo("10.1.2.3");
    }

    @Test
    @DisplayName("Проверка запрещает и запрос, и ответ: цепочка вызывается, лога нет")
    void skipsEverythingWhenCheckerDenies() throws Exception {
        HttpLoggingFilter denying = newFilter(new MethodLoggingChecker() {
            @Override
            public boolean shouldLogRequest(HttpServletRequest request) {
                return false;
            }

            @Override
            public boolean shouldLogResponse(HttpServletRequest request) {
                return false;
            }
        });
        boolean[] called = {false};

        denying.doFilter(post("/orders", "x"), new MockHttpServletResponse(), (req, res) -> called[0] = true);

        assertThat(called[0]).isTrue();
        assertThat(requests).isEmpty();
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Проверка запрещает только ответ: логируется один запрос")
    void logsOnlyRequestWhenResponseDenied() throws Exception {
        HttpLoggingFilter partial = newFilter(new MethodLoggingChecker() {
            @Override
            public boolean shouldLogRequest(HttpServletRequest request) {
                return true;
            }

            @Override
            public boolean shouldLogResponse(HttpServletRequest request) {
                return false;
            }
        });

        partial.doFilter(post("/orders", "x"), new MockHttpServletResponse(), (req, res) -> {});

        assertThat(requests).hasSize(1);
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Сжатый ответ заменяется заглушкой, если не включён forcePrintEncodedPayload")
    void encodedResponseIsReplacedWithDummy() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/data");
        request.setServletPath("/data");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            ((HttpServletResponse) res).setHeader("Content-Encoding", "gzip");
            res.getOutputStream().write(new byte[] {1, 2, 3});
        });
        assertThat(text(responses.get(0).getBody())).isEqualTo("[encoded payload]");

        filter.setForcePrintEncodedPayload(true);
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            ((HttpServletResponse) res).setHeader("Content-Encoding", "gzip");
            res.getOutputStream().write(new byte[] {1, 2, 3});
        });
        assertThat(responses.get(1).getBody()).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Исключённый content-type ответа: вместо тела пояснение")
    void excludedContentTypeIsNotLogged() throws Exception {
        filter.setResponseBodyExcludeContentTypes(List.of("application/pdf"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/report");
        request.setServletPath("/report");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            res.setContentType("application/pdf");
            res.getOutputStream().write("%PDF".getBytes(StandardCharsets.UTF_8));
        });

        assertThat(text(responses.get(0).getBody())).isEqualTo("Body logging is disabled for this content-type");
    }

    @Test
    @DisplayName("Ошибка записи лога не ломает обработку запроса")
    void writerFailureDoesNotBreakRequest() throws Exception {
        HttpLoggingFilter failing = new HttpLoggingFilter(
                new AllowAllMethodLoggingChecker(),
                log -> {
                    throw new IllegalStateException("disk full");
                },
                log -> {
                    throw new IllegalStateException("disk full");
                });
        boolean[] called = {false};
        MockHttpServletResponse response = new MockHttpServletResponse();

        failing.doFilter(post("/orders", "x"), response, (req, res) -> {
            req.getInputStream().readAllBytes();
            called[0] = true;
        });

        assertThat(called[0]).isTrue();
    }

    @Test
    @DisplayName("Исключение приложения пробрасывается, а лог ответа всё равно пишется")
    void propagatesApplicationException() {
        FilterChain failing = (req, res) -> {
            throw new IOException("boom");
        };

        assertThatThrownBy(() -> filter.doFilter(post("/orders", "x"), new MockHttpServletResponse(), failing))
                .isInstanceOf(IOException.class)
                .hasMessage("boom");
        assertThat(requests).hasSize(1);
        assertThat(responses).hasSize(1);
    }
}
