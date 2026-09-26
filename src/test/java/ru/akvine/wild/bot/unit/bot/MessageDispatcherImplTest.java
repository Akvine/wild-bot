package ru.akvine.wild.bot.unit.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.BACK_BUTTON_TEXT;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.bot.MessageDispatcherImpl;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Message;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.states.StateResolver;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.facades.CommandResolverFacade;
import ru.akvine.wild.bot.facades.StateResolverFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

/**
 * Изолированные тесты роутинга {@link MessageDispatcherImpl}: команды, первый заход в диалог,
 * кнопка "Назад" и обычный переход по текущему состоянию — без Spring-контекста, только
 * моки зависимостей.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageDispatcherImpl tests")
class MessageDispatcherImplTest {

    @Mock
    private StateStorage<String, List<ClientState>> stateStorage;

    @Mock
    private BotView mainMenuView;

    @Mock
    private CommandResolver startResolver;

    @Mock
    private StateResolver currentStateResolver;

    private MessageDispatcherImpl dispatcher;

    @BeforeEach
    void setUp() {
        BotViewFacade botViewFacade = new BotViewFacade(Map.of(ClientState.MAIN_MENU, mainMenuView));
        CommandResolverFacade commandResolverFacade =
                new CommandResolverFacade(Map.of(Command.COMMAND_START, startResolver));
        StateResolverFacade stateResolverFacade =
                new StateResolverFacade(Map.of(ClientState.MAIN_MENU, currentStateResolver));
        dispatcher = new MessageDispatcherImpl(stateStorage, botViewFacade, stateResolverFacade, commandResolverFacade);
    }

    @Test
    @DisplayName("Text starting with '/' and matching a known command delegates to its resolver")
    void doDispatch_knownCommand_delegatesToResolver() {
        Payload payload = payload("1", BotType.TELEGRAM, "/start");
        Response expected = new Response();
        when(startResolver.resolve(BotType.TELEGRAM, "1", "/start")).thenReturn(expected);

        Response actual = dispatcher.doDispatch(payload);

        assertThat(actual).isSameAs(expected);
        verifyNoInteractions(stateStorage, currentStateResolver);
    }

    @Test
    @DisplayName("Unknown command returns a friendly error without touching state resolvers")
    void doDispatch_unknownCommand_returnsFriendlyErrorMessage() {
        Payload payload = payload("1", BotType.TELEGRAM, "/does-not-exist");

        Response actual = dispatcher.doDispatch(payload);

        assertThat(actual.getText()).isEqualTo("Такой команды не существует!");
        verifyNoInteractions(currentStateResolver);
    }

    @Test
    @DisplayName("First message for a chat without state seeds MAIN_MENU and returns a Telegram response")
    void doDispatch_firstMessage_seedsMainMenuState_forTelegram() {
        Payload payload = payload("1", BotType.TELEGRAM, "hello");
        when(stateStorage.containsState("1", BotType.TELEGRAM)).thenReturn(false);
        when(mainMenuView.getMessage("1", BotType.TELEGRAM)).thenReturn("menu text");
        when(mainMenuView.getKeyboard("1", BotType.TELEGRAM))
                .thenReturn(new InlineKeyboard((InlineKeyboardMarkup) null));

        Response response = dispatcher.doDispatch(payload);

        verify(stateStorage).add("1", BotType.TELEGRAM, ClientState.MAIN_MENU);
        assertThat(response.getTelegramResponse().getText()).isEqualTo("menu text");
    }

    @Test
    @DisplayName("First message for a chat without state seeds MAIN_MENU and returns a Max response")
    void doDispatch_firstMessage_seedsMainMenuState_forMax() {
        Payload payload = payload("1", BotType.MAX, "hello");
        when(stateStorage.containsState("1", BotType.MAX)).thenReturn(false);
        when(mainMenuView.getMessage("1", BotType.MAX)).thenReturn("menu text");
        when(mainMenuView.getKeyboard("1", BotType.MAX)).thenReturn(new InlineKeyboard((Button[][]) null));

        Response response = dispatcher.doDispatch(payload);

        verify(stateStorage).add("1", BotType.MAX, ClientState.MAIN_MENU);
        assertThat(response.getMaxSendMessage().getText()).isEqualTo("menu text");
        assertThat(response.getMaxSendMessage().getAttachments()).isNull();
    }

    @Test
    @DisplayName("Back button with more than one state in history delegates to setPreviousStateForBackButton")
    void doDispatch_backButtonWithHistory_delegatesToPreviousState() {
        Payload payload = payload("1", BotType.TELEGRAM, BACK_BUTTON_TEXT);
        when(stateStorage.containsState("1", BotType.TELEGRAM)).thenReturn(true);
        when(stateStorage.statesCount("1", BotType.TELEGRAM)).thenReturn(2);
        when(stateStorage.getCurrent("1", BotType.TELEGRAM)).thenReturn(ClientState.MAIN_MENU);
        Response expected = new Response();
        when(currentStateResolver.setPreviousStateForBackButton(payload)).thenReturn(expected);

        Response actual = dispatcher.doDispatch(payload);

        assertThat(actual).isSameAs(expected);
        verify(currentStateResolver, never()).resolve(payload);
    }

    @Test
    @DisplayName("Back button text with only one state in history is treated as ordinary text")
    void doDispatch_backButtonWithoutHistory_delegatesToCurrentStateResolver() {
        Payload payload = payload("1", BotType.TELEGRAM, BACK_BUTTON_TEXT);
        when(stateStorage.containsState("1", BotType.TELEGRAM)).thenReturn(true);
        when(stateStorage.statesCount("1", BotType.TELEGRAM)).thenReturn(1);
        when(stateStorage.getCurrent("1", BotType.TELEGRAM)).thenReturn(ClientState.MAIN_MENU);
        Response expected = new Response();
        when(currentStateResolver.resolve(payload)).thenReturn(expected);

        Response actual = dispatcher.doDispatch(payload);

        assertThat(actual).isSameAs(expected);
        verify(currentStateResolver, never()).setPreviousStateForBackButton(any());
    }

    @Test
    @DisplayName("Ordinary text with an existing state history delegates to the current state resolver")
    void doDispatch_existingState_delegatesToCurrentStateResolver() {
        Payload payload = payload("1", BotType.TELEGRAM, "some text");
        when(stateStorage.containsState("1", BotType.TELEGRAM)).thenReturn(true);
        when(stateStorage.statesCount("1", BotType.TELEGRAM)).thenReturn(1);
        when(stateStorage.getCurrent("1", BotType.TELEGRAM)).thenReturn(ClientState.MAIN_MENU);
        Response expected = new Response();
        when(currentStateResolver.resolve(payload)).thenReturn(expected);

        Response actual = dispatcher.doDispatch(payload);

        assertThat(actual).isSameAs(expected);
    }

    private Payload payload(String chatId, BotType botType, String text) {
        return new Payload().setChatId(chatId).setBotType(botType).setMessage(new Message().setText(text));
    }
}
