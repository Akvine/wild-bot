package ru.akvine.wild.bot.services.integration.max;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.infrastructure.resilience.BulkheadFactory;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerInterceptorFactory;
import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.CommandDto;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.max.dto.request.UpdateCommandsRequest;

@DisplayName("MaxIntegrationServiceOrigin")
class MaxIntegrationServiceOriginTest {
    private static final String URL = "https://max.test";

    private MaxIntegrationServiceOrigin service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new MaxIntegrationServiceOrigin();
        ReflectionTestUtils.setField(service, "maxUrl", URL);
        ReflectionTestUtils.setField(service, "maxBotToken", "bot-token");
        ReflectionTestUtils.setField(service, "poolingTimeoutSeconds", "30");
        ReflectionTestUtils.setField(service, "updateTypes", "message_created,message_callback");
        CircuitBreakerInterceptorFactory circuitBreakers = mock(CircuitBreakerInterceptorFactory.class);
        BulkheadFactory bulkheads = mock(BulkheadFactory.class);
        when(bulkheads.interceptor("max")).thenReturn((request, body, execution) -> execution.execute(request, body));
        when(circuitBreakers.create("max")).thenReturn((request, body, execution) -> execution.execute(request, body));
        ReflectionTestUtils.setField(service, "circuitBreakerInterceptorFactory", circuitBreakers);
        ReflectionTestUtils.setField(service, "bulkheadFactory", bulkheads);
        service.initResilience();
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    @DisplayName("updateCommands: PATCH /me/commands с токеном бота")
    void updateCommands() {
        server.expect(requestTo(URL + "/me/commands"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "bot-token"))
                .andExpect(jsonPath("$.commands[0].name").value("start"))
                .andRespond(withSuccess("{\"commands\":[]}", MediaType.APPLICATION_JSON));

        service.updateCommands(
                new UpdateCommandsRequest().setCommands(new CommandDto[] {new CommandDto().setName("start")}));

        server.verify();
    }

    @Test
    @DisplayName("updates: long polling с timeout и types, пустой ответ - пустой массив")
    void updates() {
        server.expect(requestTo(Matchers.startsWith(URL + "/updates")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"updates\":[{\"update_type\":\"message_created\",\"timestamp\":5}],\"marker\":7}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo(Matchers.startsWith(URL + "/updates")))
                .andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON));

        assertThat(service.updates()).hasSize(1);
        assertThat(service.updates()).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("getMessages и sendMessage передают chat_id; пустое тело ответа - пустой массив")
    void messages() {
        server.expect(requestTo(Matchers.startsWith(URL + "/messages")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"messages\":[{\"body\":{\"text\":\"hi\"}}]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(Matchers.startsWith(URL + "/messages")))
                .andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON));
        server.expect(requestTo(Matchers.startsWith(URL + "/messages")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.text").value("hello"))
                .andRespond(withSuccess("{\"messages\":[]}", MediaType.APPLICATION_JSON));

        assertThat(service.getMessages("42")).hasSize(1);
        assertThat(service.getMessages("42")).isEmpty();
        service.sendMessage("42", new SendMessageRequest().setText("hello"));
        server.verify();
    }

    @Test
    @DisplayName("getUploadFileUrl и uploadFileAtServer возвращают url и токен")
    void uploads() {
        server.expect(requestTo(Matchers.startsWith(URL + "/uploads")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"url\":\"https://upload.test/x\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://upload.test/x"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"token\":\"file-token\",\"fileId\":3}", MediaType.APPLICATION_JSON));

        assertThat(service.getUploadFileUrl(AttachmentType.FILE)).isEqualTo("https://upload.test/x");
        assertThat(service.uploadFileAtServer("https://upload.test/x", new byte[] {1}, "a.bin"))
                .isEqualTo("file-token");
        server.verify();
    }

    @Test
    @DisplayName("uploadImageAtServer достаёт токен первой фотографии из ответа")
    void uploadImageReturnsFirstPhotoToken() {
        server.expect(requestTo("https://upload.test/img"))
                .andRespond(
                        withSuccess("{\"photos\":{\"p1\":{\"token\":\"photo-token\"}}}", MediaType.APPLICATION_JSON));

        assertThat(service.uploadImageAtServer("https://upload.test/img", new byte[] {1}, "a.jpg"))
                .isEqualTo("photo-token");
    }

    @Test
    @DisplayName("uploadImageAtServer: некорректные ответы превращаются в IntegrationException")
    void uploadImageRejectsBadResponses() {
        String[] bodies = {
            "{\"error_code\":\"bad\",\"error_data\":\"details\"}",
            "{\"error_code\":\"bad\"}",
            "{}",
            "{\"photos\":[]}",
            "{\"photos\":{}}",
            "{\"photos\":{\"p1\":{}}}",
            "not json"
        };
        String[] messages = {
            "code=bad, data=details", "code=bad, data=", "'photos'", "'photos'", "No entries", "'token'", "Bad response"
        };
        for (int i = 0; i < bodies.length; i++) {
            MockRestServiceServer fresh = MockRestServiceServer.bindTo(
                            (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate"))
                    .build();
            fresh.expect(requestTo("https://upload.test/img"))
                    .andRespond(withSuccess(bodies[i], MediaType.APPLICATION_JSON));

            int index = i;
            assertThatThrownBy(() -> service.uploadImageAtServer("https://upload.test/img", new byte[0], "a.jpg"))
                    .as(bodies[index])
                    .isInstanceOf(IntegrationException.class)
                    .hasMessageContaining(messages[index]);
        }
    }

    @Test
    @DisplayName("Любая ошибка MAX превращается в IntegrationException с названием метода")
    void failuresAreWrapped() {
        server.expect(ExpectedCount.manyTimes(), requestTo(Matchers.any(String.class)))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.updateCommands(new UpdateCommandsRequest()))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("UPDATE_COMMAND");
        assertThatThrownBy(() -> service.updates())
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("LONG_POOLING_SUBSCRIPTIONS_GET");
        assertThatThrownBy(() -> service.getMessages("1"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("GET_MESSAGES");
        assertThatThrownBy(() -> service.sendMessage("1", new SendMessageRequest()))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("SEND_MESSAGE");
        assertThatThrownBy(() -> service.getUploadFileUrl(AttachmentType.IMAGE))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("GET_UPLOAD_FILE_URL");
        assertThatThrownBy(() -> service.uploadFileAtServer("https://upload.test/x", new byte[0], "a"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("uploading file");
        assertThatThrownBy(() -> service.uploadImageAtServer("https://upload.test/x", new byte[0], "a"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("uploading image");
    }

    @Test
    @DisplayName("downloadAttachment скачивает файл по прямой ссылке; недоступная ссылка - IntegrationException")
    void downloadAttachment() throws IOException {
        HttpServer fileServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fileServer.createContext("/file", exchange -> {
            byte[] body = "file-content".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        fileServer.start();
        try {
            String url = "http://127.0.0.1:" + fileServer.getAddress().getPort() + "/file";

            assertThat(new String(service.downloadAttachment(url), StandardCharsets.UTF_8))
                    .isEqualTo("file-content");
        } finally {
            fileServer.stop(0);
        }

        assertThatThrownBy(() -> service.downloadAttachment("http://127.0.0.1:1/nothing"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("downloading MAX attachment");
        assertThatThrownBy(() -> service.downloadAttachment("not a url")).isInstanceOf(IntegrationException.class);
    }
}
