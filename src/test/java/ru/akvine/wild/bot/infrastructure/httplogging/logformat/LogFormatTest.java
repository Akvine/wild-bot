package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer.Slf4jHttpLogWriter;

@DisplayName("Форматирование и запись HTTP-логов")
class LogFormatTest {
    private final SimpleLogFormatter formatter = new SimpleLogFormatter(Set.of("Authorization", "secret"), Set.of());

    private static RequestLog request() {
        RequestLog log = new RequestLog();
        log.setHttpRequestMethod("POST");
        log.setRequestURI("/orders");
        log.setEncoding("UTF-8");
        return log;
    }

    @Test
    @DisplayName("Запрос: метод, uri, query, адрес, сессия, пользователь и тело")
    void formatsFullRequest() {
        RequestLog log = request();
        log.setQueryString("a=1");
        log.setRemoteAddr("10.0.0.1");
        log.setSessionId("S1");
        log.setUser("john");
        log.setBody("тело".getBytes(StandardCharsets.UTF_8));

        String text = formatter.format(log);

        assertThat(text)
                .contains("POST /orders")
                .contains("?a=1")
                .contains("remoteAddr: 10.0.0.1")
                .contains("sessionId: S1")
                .contains("user: john")
                .contains("body:\nтело");
    }

    @Test
    @DisplayName("Заголовки из чёрного списка и api-key маскируются, из белого списка - нет")
    void masksSensitiveHeaders() {
        SimpleLogFormatter withWhite = new SimpleLogFormatter(Set.of("Authorization"), Set.of("Authorization-Public"));
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Authorization", List.of("Bearer abcdef"));
        headers.put("Authorization-Public", List.of("Bearer visible"));
        headers.put("X-Api-Key", List.of("12345678"));
        headers.put("X-Short", List.of("abc"));
        headers.put("Accept", List.of("json"));
        RequestLog log = request();
        log.setHttpHeaders(headers);

        String text = withWhite.format(log);

        assertThat(text)
                .contains("Authorization: [B***********f]")
                .contains("Authorization-Public: [Bearer visible]")
                .contains("X-Api-Key: [1******8]")
                .contains("X-Short: [abc]")
                .contains("Accept: [json]");
        assertThat(text).doesNotContain("abcdef").doesNotContain("12345678");

        headers.put("secret-key", List.of("abc"));
        assertThat(formatter.format(log)).contains("secret-key: [****]");
    }

    @Test
    @DisplayName("Ответ: статус, заголовки и тело")
    void formatsResponse() {
        ResponseLog log = new ResponseLog();
        log.setStatus(404);
        log.setEncoding("UTF-8");
        log.setHttpHeaders(Map.of("X-Id", List.of("7")));
        log.setBody("not found".getBytes(StandardCharsets.UTF_8));

        String text = formatter.format(log);

        assertThat(text).contains("status: 404").contains("X-Id: [7]").contains("body:\nnot found");
        assertThat(log.toString()).contains("404");
    }

    @Test
    @DisplayName("Тело без известной кодировки: заглушка или hex")
    void bodyWithUnknownEncoding() {
        RequestLog log = new RequestLog();
        log.setBody(new byte[] {0x0a, (byte) 0xff});
        assertThat(log.bodyAsString()).isEqualTo("[unknown encoding]");

        log.setUnknownToHex(true);
        assertThat(log.bodyAsString()).isEqualTo("0aff");

        log.setEncoding("no-such-charset");
        assertThat(log.bodyAsString()).isEqualTo("0aff");

        log.setUnknownToHex(false);
        assertThat(log.bodyAsString()).isEqualTo("[unknown encoding]");

        log.setBody(null);
        assertThat(log.bodyAsString()).isEmpty();
    }

    @Test
    @DisplayName("Форматтер без параметров не маскирует заголовки и допускает пустое тело")
    void defaultFormatter() {
        SimpleLogFormatter plain = new SimpleLogFormatter();
        RequestLog log = request();
        log.setHttpHeaders(Map.of("Accept", List.of("json")));

        assertThat(plain.format(log)).contains("Accept: [json]").doesNotContain("body:");
    }

    @Test
    @DisplayName("Slf4jHttpLogWriter пишет в логгер с заданным именем и уровнем")
    void writerWritesToConfiguredLogger() {
        Logger logger = (Logger) LoggerFactory.getLogger("http.test.logger");
        logger.setLevel(ch.qos.logback.classic.Level.TRACE);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            for (Level level : Level.values()) {
                new Slf4jHttpLogWriter(formatter, formatter, "http.test.logger", level).log(request());
            }
            new Slf4jHttpLogWriter(formatter, formatter, "http.test.logger", null).log(new ResponseLog());

            assertThat(appender.list)
                    .extracting(ILoggingEvent::getLevel)
                    .containsExactly(
                            ch.qos.logback.classic.Level.ERROR,
                            ch.qos.logback.classic.Level.WARN,
                            ch.qos.logback.classic.Level.INFO,
                            ch.qos.logback.classic.Level.DEBUG,
                            ch.qos.logback.classic.Level.TRACE,
                            ch.qos.logback.classic.Level.DEBUG);
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("Остальные конструкторы writer'а используют логгер по умолчанию")
    void writerConstructors() {
        new Slf4jHttpLogWriter(formatter).log(request());
        new Slf4jHttpLogWriter(formatter, "some.name").log(request());
        new Slf4jHttpLogWriter(formatter, formatter).log(new ResponseLog());
    }
}
