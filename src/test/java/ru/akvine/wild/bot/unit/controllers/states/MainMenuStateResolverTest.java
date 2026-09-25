package ru.akvine.wild.bot.unit.controllers.states;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Message;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.states.MainMenuStateResolver;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.exceptions.HasNoSubscriptionException;
import ru.akvine.wild.bot.exceptions.SubscriptionExpiredException;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.SubscriptionService;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.property.PropertyService;

/**
 * Изолированные тесты ветвления главного меню: доступ к "Мои тесты"/"Инструкции" зависит от
 * подписки, а "Оформить подписку"/"Настройки аккаунта" - нет. Без Spring-контекста и БД.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MainMenuStateResolver tests")
class MainMenuStateResolverTest {
    private static final String CHAT_ID = "1";

    @Mock
    private StateStorage<String, List<ClientState>> stateStorage;

    @Mock
    private TelegramIntegrationService telegramIntegrationService;

    @Mock
    private PropertyService propertyService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private BotView view;

    private MainMenuStateResolver resolver;

    @BeforeEach
    void setUp() {
        BotViewFacade viewFacade = new BotViewFacade(Map.of(
                ClientState.TESTS_MENU, view,
                ClientState.INSTRUCTIONS_MENU, view,
                ClientState.SUBSCRIBE_MENU, view,
                ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU, view));
        resolver = new MainMenuStateResolver(
                stateStorage, viewFacade, subscriptionService, telegramIntegrationService, propertyService);

        lenient().when(view.getMessage(any(), any())).thenReturn("text");
        lenient().when(view.getKeyboard(any(), any())).thenReturn(new InlineKeyboard((InlineKeyboardMarkup) null));
    }

    @Test
    @DisplayName("Tapping 'Мои тесты' with an active subscription transitions to TESTS_MENU")
    void resolve_testsButtonWithActiveSubscription_transitionsToTestsMenu() {
        when(subscriptionService.getByChatIdOrNull(CHAT_ID)).thenReturn(activeSubscription());

        Response response = resolver.resolve(payload(TESTS_MENU));

        verify(stateStorage).add(CHAT_ID, BotType.TELEGRAM, ClientState.TESTS_MENU);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Tapping 'Инструкция по использованию' without a subscription throws HasNoSubscriptionException")
    void resolve_instructionsButtonWithoutSubscription_throwsHasNoSubscriptionException() {
        when(subscriptionService.getByChatIdOrNull(CHAT_ID)).thenReturn(null);

        assertThatThrownBy(() -> resolver.resolve(payload(INSTRUCTIONS_FOR_USE_BUTTON_TEXT)))
                .isInstanceOf(HasNoSubscriptionException.class);
        verify(stateStorage, never()).add(any(), any(), eq(ClientState.INSTRUCTIONS_MENU));
    }

    @Test
    @DisplayName("Tapping 'Мои тесты' with an expired subscription throws SubscriptionExpiredException")
    void resolve_testsButtonWithExpiredSubscription_throwsSubscriptionExpiredException() {
        when(subscriptionService.getByChatIdOrNull(CHAT_ID)).thenReturn(expiredSubscription());

        assertThatThrownBy(() -> resolver.resolve(payload(TESTS_MENU)))
                .isInstanceOf(SubscriptionExpiredException.class);
        verify(stateStorage, never()).add(any(), any(), eq(ClientState.TESTS_MENU));
    }

    @Test
    @DisplayName("'Оформить подписку' doesn't require an existing subscription")
    void resolve_subscribeButton_doesNotCheckSubscription() {
        resolver.resolve(payload(ADD_SUBSCRIPTION_BUTTON_TEXT));

        verify(stateStorage).add(CHAT_ID, BotType.TELEGRAM, ClientState.SUBSCRIBE_MENU);
        verifyNoInteractions(subscriptionService);
    }

    @Test
    @DisplayName("'Настройки аккаунта' doesn't require an existing subscription")
    void resolve_accountSettingsButton_doesNotCheckSubscription() {
        resolver.resolve(payload(WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT));

        verify(stateStorage).add(CHAT_ID, BotType.TELEGRAM, ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);
        verifyNoInteractions(subscriptionService);
    }

    @Test
    @DisplayName("Unrecognized text returns the default 'choose an action' response")
    void resolve_unknownText_returnsDefaultResponse() {
        Response response = resolver.resolve(payload("gibberish"));

        assertThat(response.getTelegramResponse().getText()).isEqualTo("Необходимо выбрать действие из меню!");
        verifyNoInteractions(stateStorage);
    }

    private SubscriptionModel activeSubscription() {
        return subscription(LocalDateTime.now().plusDays(1));
    }

    private SubscriptionModel expiredSubscription() {
        return subscription(LocalDateTime.now().minusDays(1));
    }

    private SubscriptionModel subscription(LocalDateTime expiresAt) {
        ClientEntity client = new ClientEntity()
                .setUuid("uuid")
                .setChatId(CHAT_ID)
                .setFirstName("F")
                .setBotType(BotType.TELEGRAM);
        SubscriptionEntity entity = new SubscriptionEntity().setClient(client).setExpiresAt(expiresAt);
        return new SubscriptionModel(entity);
    }

    private Payload payload(String text) {
        return new Payload()
                .setChatId(CHAT_ID)
                .setBotType(BotType.TELEGRAM)
                .setBotDataType(BotDataType.CALLBACK)
                .setMessage(new Message().setText(text));
    }
}
