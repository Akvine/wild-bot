package ru.akvine.wild.bot.services.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.infrastructure.retry.DefaultRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.RetryExecutor;
import ru.akvine.wild.bot.services.integration.custodian.CustodianPropertyServiceIntegration;
import ru.akvine.wild.bot.services.integration.custodian.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.qrcode.QRaftIntegrationService;
import ru.akvine.wild.bot.services.integration.qrcode.dto.GenerateQrCodeRequest;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationServiceOrigin;
import ru.akvine.wild.bot.services.integration.trimly.TrimlyIntegrationOriginService;
import ru.akvine.wild.bot.telegram.bot.TelegramDevBot;
import ru.akvine.wild.bot.telegram.bot.TelegramProductionBot;

@DisplayName("Клиенты внешних сервисов: Custodian, QRaft, Trimly, Telegram и адаптер ботов")
class ExternalClientsTest {

    private static MockRestServiceServer bind(Object service) {
        return MockRestServiceServer.bindTo((RestTemplate) ReflectionTestUtils.getField(service, "restTemplate"))
                .build();
    }

    @Test
    @DisplayName("Custodian: POST /properties с Bearer-токеном и разбор списка настроек")
    void custodianGetProperties() {
        CustodianPropertyServiceIntegration service = new CustodianPropertyServiceIntegration();
        ReflectionTestUtils.setField(service, "url", "https://custodian.test");
        ReflectionTestUtils.setField(service, "token", "secret");
        MockRestServiceServer server = bind(service);
        server.expect(requestTo("https://custodian.test/properties"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer secret"))
                .andExpect(jsonPath("$.profile").value("prod"))
                .andRespond(withSuccess("{\"count\":0,\"properties\":[]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://custodian.test/properties")).andRespond(withServerError());

        assertThat(service.getProperties(new GetPropertiesRequest().setProfile("prod"))
                        .getCount())
                .isZero();
        assertThatThrownBy(() -> service.getProperties(new GetPropertiesRequest()))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("PROPERTIES");
    }

    @Test
    @DisplayName("QRaft: возвращает байты картинки; ошибка превращается в IntegrationException")
    void qraftGeneratesQrCode() {
        QRaftIntegrationService service = new QRaftIntegrationService();
        ReflectionTestUtils.setField(service, "url", "https://qraft.test");
        ReflectionTestUtils.setField(service, "method", "/generate");
        MockRestServiceServer server = bind(service);
        server.expect(requestTo("https://qraft.test/generate"))
                .andExpect(jsonPath("$.url").value("https://wb.ru"))
                .andRespond(withSuccess(new byte[] {1, 2, 3}, MediaType.IMAGE_PNG));
        server.expect(requestTo("https://qraft.test/generate")).andRespond(withServerError());

        assertThat(service.generateQrCode(new GenerateQrCodeRequest().setUrl("https://wb.ru")))
                .containsExactly(1, 2, 3);
        assertThatThrownBy(() -> service.generateQrCode(new GenerateQrCodeRequest()))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("GENERATE");
        assertThat(service.getType()).isNotNull();
    }

    @Test
    @DisplayName("Trimly: создаёт короткую ссылку; ошибка превращается в IntegrationException")
    void trimlyCreatesShortUrl() {
        TrimlyIntegrationOriginService service = new TrimlyIntegrationOriginService();
        ReflectionTestUtils.setField(service, "url", "https://trimly.test");
        ReflectionTestUtils.setField(service, "method", "/short");
        MockRestServiceServer server = bind(service);
        server.expect(requestTo("https://trimly.test/short"))
                .andExpect(jsonPath("$.originUrl").value("https://long.example/x"))
                .andRespond(withSuccess("{\"shortUrl\":\"https://t.ly/a\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://trimly.test/short")).andRespond(withServerError());

        assertThat(service.createTempShortUrl("https://long.example/x").getShortUrl())
                .isEqualTo("https://t.ly/a");
        assertThatThrownBy(() -> service.createTempShortUrl("https://long.example/y"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("SHORT");
    }

    private TelegramIntegrationServiceOrigin telegram(
            String botType, TelegramDevBot dev, TelegramProductionBot prod, AbsSender sender) {
        TelegramIntegrationServiceOrigin service = new TelegramIntegrationServiceOrigin();
        ReflectionTestUtils.setField(service, "botToken", "token");
        ReflectionTestUtils.setField(service, "botType", botType);
        service.setBot(dev);
        service.setAbsSender(sender);
        ReflectionTestUtils.setField(service, "bot", botType.equals("longpooling") ? dev : prod);
        return service;
    }

    @Test
    @DisplayName("Telegram: сообщения отправляются тем ботом, который соответствует режиму (long polling или webhook)")
    void telegramSendsMessagesThroughConfiguredBot() throws Exception {
        TelegramDevBot dev = mock(TelegramDevBot.class);
        TelegramProductionBot prod = mock(TelegramProductionBot.class);
        AbsSender sender = mock(AbsSender.class);

        telegram("longpooling", dev, prod, sender).sendMessage(Set.of("1", "2"), "hello");
        telegram("webhook", dev, prod, sender).sendMessage("3", "hi");

        verify(dev, times(2)).execute(any(SendMessage.class));
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(prod).execute(captor.capture());
        assertThat(captor.getValue().getChatId()).isEqualTo("3");
        assertThat(captor.getValue().getText()).isEqualTo("hi");
    }

    @Test
    @DisplayName("Telegram: файлы, картинки и ответ на callback уходят через AbsSender")
    void telegramSendsFilesAndImages() throws Exception {
        AbsSender sender = mock(AbsSender.class);
        TelegramIntegrationServiceOrigin service =
                telegram("webhook", mock(TelegramDevBot.class), mock(TelegramProductionBot.class), sender);

        service.sendFile("1", "report.xlsx", new byte[] {1});
        service.sendFile("1", "report.xlsx", new ByteArrayInputStream(new byte[] {1}));
        service.sendImage("1", new byte[] {1});
        service.sendImage("1", new ByteArrayInputStream(new byte[] {1}));
        service.sendImage("1", new byte[] {1}, "caption");
        service.sendImage("1", new ByteArrayInputStream(new byte[] {1}), "  ");
        service.answerCallback(BotDataType.CALLBACK, "cb-1");
        service.answerCallback(BotDataType.MESSAGE, "cb-2");

        verify(sender, times(2)).execute(any(SendDocument.class));
        ArgumentCaptor<SendPhoto> photos = ArgumentCaptor.forClass(SendPhoto.class);
        verify(sender, times(4)).execute(photos.capture());
        assertThat(photos.getAllValues())
                .extracting(SendPhoto::getCaption)
                .containsExactly(null, null, "caption", null);
        ArgumentCaptor<AnswerCallbackQuery> callback = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(sender).execute(callback.capture());
        assertThat(callback.getValue().getCallbackQueryId()).isEqualTo("cb-1");
    }

    @Test
    @DisplayName("Telegram: ошибки API превращаются в IntegrationException с названием метода")
    void telegramWrapsErrors() throws Exception {
        TelegramDevBot dev = mock(TelegramDevBot.class);
        TelegramProductionBot prod = mock(TelegramProductionBot.class);
        AbsSender sender = mock(AbsSender.class);
        doThrow(new RuntimeException("boom")).when(sender).execute(any(SendDocument.class));
        doThrow(new RuntimeException("boom")).when(sender).execute(any(SendPhoto.class));
        doThrow(new RuntimeException("boom")).when(sender).execute(any(AnswerCallbackQuery.class));
        doThrow(new RuntimeException("boom")).when(prod).execute(any(SendMessage.class));
        doThrow(new RuntimeException("boom")).when(prod).execute(any(GetFile.class));
        doThrow(new RuntimeException("boom")).when(dev).execute(any(GetFile.class));
        TelegramIntegrationServiceOrigin webhook = telegram("webhook", dev, prod, sender);
        TelegramIntegrationServiceOrigin polling = telegram("longpooling", dev, prod, sender);

        assertThatThrownBy(() -> webhook.sendFile("1", "f", new byte[0])).hasMessageContaining("SEND_FILE");
        assertThatThrownBy(() -> webhook.sendImage("1", new byte[0])).hasMessageContaining("SEND_IMAGE");
        assertThatThrownBy(() -> webhook.sendMessage(Set.of("1"), "m")).hasMessageContaining("SEND_MESSAGE");
        assertThatThrownBy(() -> webhook.answerCallback(BotDataType.CALLBACK, "cb"))
                .hasMessageContaining("ANSWER_CALLBACK");
        assertThatThrownBy(() -> webhook.downloadPhoto("photo", "1")).hasMessageContaining("DOWNLOAD_PHOTO");
        assertThatThrownBy(() -> polling.downloadPhoto("photo", "1")).hasMessageContaining("DOWNLOAD_PHOTO");
        assertThatThrownBy(() -> webhook.downloadPhoto(null, "1")).isInstanceOf(NullPointerException.class);
    }

    private BotIntegrationAdapterImpl adapter(
            TelegramIntegrationService telegram, MaxIntegrationService max, RetryExecutor retry) {
        return new BotIntegrationAdapterImpl(telegram, max, retry);
    }

    @Test
    @DisplayName("Адаптер: Telegram-клиенту всё уходит напрямую в Telegram-сервис")
    void adapterRoutesTelegram() {
        TelegramIntegrationService telegram = mock(TelegramIntegrationService.class);
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        BotIntegrationAdapterImpl adapter = adapter(telegram, max, new DefaultRetryExecutor(3, Duration.ofMillis(1)));

        adapter.sendMessage(Set.of("1"), BotType.TELEGRAM, "text");
        adapter.sendImage("1", BotType.TELEGRAM, new byte[] {1}, "cap");
        adapter.sendFile("1", BotType.TELEGRAM, new byte[] {2}, "f.txt");

        verify(telegram).sendMessage(Set.of("1"), "text");
        verify(telegram).sendImage("1", new byte[] {1}, "cap");
        verify(telegram).sendFile("1", "f.txt", new byte[] {2});
        verify(max, never()).sendMessage(anyString(), any());
    }

    @Test
    @DisplayName("Адаптер: в Max файл и картинка сначала загружаются на сервер, затем уходят вложением")
    void adapterRoutesMax() {
        TelegramIntegrationService telegram = mock(TelegramIntegrationService.class);
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        when(max.getUploadFileUrl(AttachmentType.IMAGE)).thenReturn("https://up/img");
        when(max.getUploadFileUrl(AttachmentType.FILE)).thenReturn("https://up/file");
        when(max.uploadImageAtServer("https://up/img", new byte[] {1}, "cap")).thenReturn("img-token");
        when(max.uploadFileAtServer("https://up/file", new byte[] {2}, "f.txt")).thenReturn("file-token");
        BotIntegrationAdapterImpl adapter = adapter(telegram, max, new DefaultRetryExecutor(3, Duration.ofMillis(1)));

        adapter.sendMessage(Set.of("10", "11"), BotType.MAX, "text");
        adapter.sendImage("10", BotType.MAX, new byte[] {1}, "cap");
        adapter.sendFile("10", BotType.MAX, new byte[] {2}, "f.txt");

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(max, times(4)).sendMessage(anyString(), captor.capture());
        assertThat(captor.getAllValues().get(2).getAttachments()[0].getPayload().getToken())
                .isEqualTo("img-token");
        assertThat(captor.getAllValues().get(3).getAttachments()[0].getPayload().getToken())
                .isEqualTo("file-token");
        verify(telegram, never()).sendMessage(any(Set.class), anyString());
    }

    @Test
    @DisplayName("Адаптер: ошибка 4xx (кроме 429) не повторяется, а временная ошибка повторяется")
    void adapterRetriesOnlyTransientErrors() {
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        when(max.getUploadFileUrl(AttachmentType.IMAGE)).thenReturn("https://up/img");
        when(max.uploadImageAtServer(anyString(), any(), anyString())).thenReturn("token");
        BotIntegrationAdapterImpl adapter =
                adapter(mock(TelegramIntegrationService.class), max, new DefaultRetryExecutor(3, Duration.ofMillis(1)));

        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(max).sendMessage(anyString(), any());
        assertThatThrownBy(() -> adapter.sendImage("1", BotType.MAX, new byte[0], "c"))
                .isNotNull();
        verify(max, times(1)).sendMessage(anyString(), any());

        org.mockito.Mockito.reset(max);
        when(max.getUploadFileUrl(AttachmentType.IMAGE)).thenReturn("https://up/img");
        when(max.uploadImageAtServer(anyString(), any(), anyString())).thenReturn("token");
        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .doNothing()
                .when(max)
                .sendMessage(anyString(), any());
        adapter.sendImage("1", BotType.MAX, new byte[0], "c");
        verify(max, times(2)).sendMessage(anyString(), any());
    }
}
