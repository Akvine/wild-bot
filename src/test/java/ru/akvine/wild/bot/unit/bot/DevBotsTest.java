package ru.akvine.wild.bot.unit.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.bot.converter.BotDtoConverter;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.bot.BotTypeNotSupportedException;
import ru.akvine.wild.bot.facades.BotDtoConverterFacade;
import ru.akvine.wild.bot.max.bot.MaxDevBot;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Recipient;
import ru.akvine.wild.bot.services.integration.max.dto.UpdateMessage;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.max.dto.request.UpdateCommandsRequest;
import ru.akvine.wild.bot.telegram.bot.TelegramDevBot;

@DisplayName("Боты для разработки: Max long polling и Telegram long polling")
@SuppressWarnings({"unchecked", "rawtypes"})
class DevBotsTest {
    private final InitMessageFilter filter = mock(InitMessageFilter.class);
    private final BotDtoConverter converter = mock(BotDtoConverter.class);
    private BotDtoConverterFacade facade;

    @BeforeEach
    void setUp() {
        facade = new BotDtoConverterFacade(Map.of(BotType.MAX, converter, BotType.TELEGRAM, converter));
    }

    private static ru.akvine.wild.bot.services.integration.max.dto.Update maxUpdate(String chatId) {
        ru.akvine.wild.bot.services.integration.max.dto.Update update =
                new ru.akvine.wild.bot.services.integration.max.dto.Update();
        update.setUpdateMessage(new UpdateMessage().setRecipient(new Recipient().setChatId(chatId)));
        return update;
    }

    @Test
    @DisplayName("Max: обновление проходит через фильтры и ответ отправляется в чат")
    void maxProcessesUpdateAndSendsResponse() {
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        MaxDevBot bot = new MaxDevBot(max, filter, facade);
        when(max.getMessages("42")).thenReturn(new Message[] {new Message()});
        Payload payload = new Payload().setChatId("42").setBotType(BotType.MAX);
        Response response = new Response("42", BotType.MAX);
        SendMessageRequest request = new SendMessageRequest().setText("hello");
        when(converter.fromRequest(any())).thenReturn(payload);
        when(filter.handle(payload)).thenReturn(response);
        when(converter.toResponse(response)).thenReturn(request);

        SendMessageRequest result =
                bot.onUpdateReceived(new ru.akvine.wild.bot.services.integration.max.dto.Update[] {maxUpdate("42")});

        assertThat(result).isSameAs(request);
        verify(max).sendMessage("42", request);
    }

    @Test
    @DisplayName("Max: игнорируемое сообщение, пустые сообщения и пустой список обновлений ничего не отправляют")
    void maxIgnoredAndEmptyUpdates() {
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        MaxDevBot bot = new MaxDevBot(max, filter, facade);

        assertThat(bot.onUpdateReceived(new ru.akvine.wild.bot.services.integration.max.dto.Update[0])
                        .getText())
                .isNull();

        when(max.getMessages("42")).thenReturn(new Message[0]);
        assertThat(bot.onUpdateReceived(new ru.akvine.wild.bot.services.integration.max.dto.Update[] {maxUpdate("42")})
                        .getText())
                .isNull();

        when(max.getMessages("42")).thenReturn(new Message[] {new Message()});
        Payload payload = new Payload().setChatId("42").setBotType(BotType.MAX);
        when(converter.fromRequest(any())).thenReturn(payload);
        when(filter.handle(payload)).thenReturn(Response.ignored("42", BotType.MAX));
        bot.onUpdateReceived(new ru.akvine.wild.bot.services.integration.max.dto.Update[] {maxUpdate("42")});

        verify(max, never()).sendMessage(any(), any());
    }

    @Test
    @DisplayName("Max: опрос обрабатывает обновления, сбой опроса не останавливает бота; команды регистрируются")
    void maxPollingAndCommands() {
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        MaxDevBot bot = new MaxDevBot(max, filter, facade);
        when(max.updates())
                .thenReturn(new ru.akvine.wild.bot.services.integration.max.dto.Update[0])
                .thenThrow(new IllegalStateException("api down"));

        ReflectionTestUtils.invokeMethod(bot, "pollAndProcess");
        ReflectionTestUtils.invokeMethod(bot, "pollAndProcess");

        bot.initCommands();
        org.mockito.ArgumentCaptor<UpdateCommandsRequest> commands =
                org.mockito.ArgumentCaptor.forClass(UpdateCommandsRequest.class);
        verify(max).updateCommands(commands.capture());
        assertThat(commands.getValue().getCommands()).hasSize(3);
    }

    @Test
    @DisplayName("Max: опрос стартует по расписанию и останавливается при уничтожении бина")
    void maxLifecycle() {
        MaxIntegrationService max = mock(MaxIntegrationService.class);
        when(max.updates()).thenReturn(new ru.akvine.wild.bot.services.integration.max.dto.Update[0]);
        MaxDevBot bot = new MaxDevBot(max, filter, facade);
        ReflectionTestUtils.setField(bot, "pollingDelayMillis", 60_000L);

        bot.init();
        bot.destroy();
        new MaxDevBot(max, filter, facade).destroy();
    }

    @Test
    @DisplayName("Telegram: обновление проходит через фильтры, ответ отправляется; игнорируемое - нет")
    void telegramProcessesUpdate() throws Exception {
        TelegramDevBot bot =
                spy(new TelegramDevBot(new DefaultBotOptions(), "123:token", "bot_username", filter, facade));
        doReturn(null).when(bot).execute(any(SendMessage.class));
        Payload payload = new Payload().setChatId("1").setBotType(BotType.TELEGRAM);
        Response response = new Response("1", BotType.TELEGRAM);
        SendMessage message = new SendMessage("1", "hi");
        when(converter.fromRequest(any())).thenReturn(payload);
        when(filter.handle(payload)).thenReturn(response);
        when(converter.toResponse(response)).thenReturn(message);

        bot.onUpdateReceived(new Update());

        verify(bot).execute(message);
        assertThat(bot.getBotUsername()).isEqualTo("bot_username");

        when(filter.handle(payload)).thenReturn(Response.ignored("1", BotType.TELEGRAM));
        bot.onUpdateReceived(new Update());
        verify(bot, org.mockito.Mockito.times(1)).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Telegram: сбой отправки превращается в RuntimeException с причиной")
    void telegramSendFailure() throws Exception {
        TelegramDevBot bot = spy(new TelegramDevBot(new DefaultBotOptions(), "123:token", "bot", filter, facade));
        org.mockito.Mockito.doThrow(new IllegalStateException("network"))
                .when(bot)
                .execute(any(SendMessage.class));

        assertThatThrownBy(() -> bot.sendMessage(new SendMessage("1", "hi")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("network");
    }

    @Test
    @DisplayName("Фасада конвертеров: неподдерживаемый тип бота - BotTypeNotSupportedException")
    void facadeRejectsUnknownType() {
        BotDtoConverterFacade onlyTelegram = new BotDtoConverterFacade(Map.of(BotType.TELEGRAM, converter));

        assertThat(onlyTelegram.getConverter(BotType.TELEGRAM)).isSameAs(converter);
        assertThatThrownBy(() -> onlyTelegram.getConverter(BotType.MAX))
                .isInstanceOf(BotTypeNotSupportedException.class);
    }
}
