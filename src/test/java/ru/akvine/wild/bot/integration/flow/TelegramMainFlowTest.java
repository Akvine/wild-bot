package ru.akvine.wild.bot.integration.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static ru.akvine.wild.bot.constants.telegram.BotMessageErrorConstants.*;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.integration.base.TelegramBaseTest;
import ru.akvine.wild.bot.integration.base.UpdateBuilder;
import ru.akvine.wild.bot.integration.config.TestConstants;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.encryption.EncryptionService;
import ru.akvine.wild.bot.utils.UUIDGenerator;

/**
 * Основной флоу Telegram-бота через реальный фильтр-пайплайн и БД (H2): /start, переходы по
 * главному меню, проверка подписки, привязка токена Wildberries и кнопка "Назад". Из внешних
 * границ замокан только {@code TelegramIntegrationService} (см. {@link TelegramBaseTest}) -
 * все остальное (фильтры, диспетчер, резолверы состояний, сервисы, репозитории) реальное.
 */
@ExtendWith(SpringExtension.class)
@DisplayName("Telegram main flow tests")
class TelegramMainFlowTest extends TelegramBaseTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private StateStorage<String, List<ClientState>> stateStorage;

    @Autowired
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        builder = new UpdateBuilder();
    }

    @Test
    @DisplayName("/start opens the main menu and remembers MAIN_MENU state")
    void start_opensMainMenu() {
        String chatId = TestConstants.CHAT_ID_2;

        BotApiMethod<?> apiMethod = sendText(chatId, "/start");

        assertThat(apiMethod).isInstanceOf(SendMessage.class);
        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM)).isEqualTo(ClientState.MAIN_MENU);
    }

    @Test
    @DisplayName("Main menu: 'Мои тесты' with an active subscription switches to TESTS_MENU")
    void mainMenu_testsButton_withActiveSubscription_transitionsToTestsMenu() {
        String chatId = TestConstants.CHAT_ID_3;
        sendText(chatId, "/start");

        sendCallback(chatId, TESTS_MENU);

        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM)).isEqualTo(ClientState.TESTS_MENU);
    }

    @Test
    @DisplayName("Main menu: 'Мои тесты' without a subscription returns the no-subscription message")
    void mainMenu_testsButton_withoutSubscription_returnsNoSubscriptionMessage() {
        String chatId = seedWhitelistedClient(false, null);
        sendText(chatId, "/start");

        BotApiMethod<?> apiMethod = sendCallback(chatId, TESTS_MENU);

        assertThat(text(apiMethod)).isEqualTo(CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE);
        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM)).isEqualTo(ClientState.MAIN_MENU);
    }

    @Test
    @DisplayName("Main menu: 'Мои тесты' with an expired subscription returns the expired message")
    void mainMenu_testsButton_withExpiredSubscription_returnsExpiredMessage() {
        String chatId = seedWhitelistedClient(true, LocalDateTime.now().minusDays(1));
        sendText(chatId, "/start");

        BotApiMethod<?> apiMethod = sendCallback(chatId, TESTS_MENU);

        assertThat(text(apiMethod)).isEqualTo(CLIENT_SUBSCRIPTION_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("WB token: revoke then enter a valid token - persisted encrypted and confirmed")
    void wildberriesToken_revokeThenInput_updatesEncryptedTokenAndConfirms() {
        String chatId = seedWhitelistedClient(false, null);
        openTokenInputMenu(chatId);

        String newToken = "AABBCCDD00112233AABBCCDD00112233";
        sendText(chatId, newToken);

        // "Токен успешно обновлен!" уходит отдельным сообщением через BotIntegrationAdapter,
        // а не в ответе на само обновление (там - сообщение меню, в которое произошёл откат).
        verify(telegramIntegrationService).sendMessage(Set.of(chatId), "Токен успешно обновлен!");
        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM))
                .isEqualTo(ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);

        ClientEntity persisted = clientRepository
                .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                .orElseThrow();
        assertThat(persisted.getToken()).isNotNull();
        assertThat(encryptionService.decrypt(persisted.getToken())).isEqualTo(newToken);
    }

    @Test
    @DisplayName("WB token: text that doesn't look like a token is rejected, state stays the same")
    void wildberriesToken_invalidFormat_isRejectedAndStateUnchanged() {
        String chatId = seedWhitelistedClient(false, null);
        openTokenInputMenu(chatId);

        BotApiMethod<?> apiMethod = sendText(chatId, "not-a-token");

        assertThat(text(apiMethod)).isEqualTo("Не похоже на токен. Попробуйте еще раз!");
        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM))
                .isEqualTo(ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU);
        assertThat(clientRepository
                        .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                        .orElseThrow()
                        .getToken())
                .isNull();
    }

    @Test
    @DisplayName("Back button returns to the previous menu")
    void backButton_returnsToPreviousState() {
        String chatId = seedWhitelistedClient(false, null);
        sendText(chatId, "/start");
        sendCallback(chatId, WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);
        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM))
                .isEqualTo(ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);

        sendCallback(chatId, BACK_BUTTON_TEXT);

        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM)).isEqualTo(ClientState.MAIN_MENU);
    }

    @Test
    @DisplayName("Unknown /command returns a friendly error instead of crashing")
    void unknownCommand_returnsNotExistsMessage() {
        String chatId = TestConstants.CHAT_ID_4;

        BotApiMethod<?> apiMethod = sendText(chatId, "/does-not-exist");

        assertThat(text(apiMethod)).isEqualTo("Такой команды не существует!");
    }

    @Test
    @DisplayName("/help lists the available commands")
    void helpCommand_listsAvailableCommands() {
        String chatId = TestConstants.CHAT_ID_5;

        BotApiMethod<?> apiMethod = sendText(chatId, "/help");

        assertThat(text(apiMethod)).contains("/start", "/help");
    }

    @Test
    @DisplayName("Soft-deleted client falls back to the generic error message")
    void deletedClient_fallsBackToGenericErrorMessage() {
        String chatId = randomNumericChatId();
        ClientEntity client = new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setFirstName("F")
                .setBotType(BotType.TELEGRAM)
                .setInWhitelist(true);
        client.setDeleted(true);
        clientRepository.save(client);

        BotApiMethod<?> apiMethod = sendText(chatId, "hello");

        assertThat(text(apiMethod)).contains("неизвестная ошибка");
    }

    private void openTokenInputMenu(String chatId) {
        sendText(chatId, "/start");
        sendCallback(chatId, WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);
        sendCallback(chatId, REVOKE_TOKEN_BUTTON_TEXT);
        assertThat(stateStorage.getCurrent(chatId, BotType.TELEGRAM))
                .isEqualTo(ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU);
    }

    private String seedWhitelistedClient(boolean withSubscription, LocalDateTime expiresAt) {
        String chatId = randomNumericChatId();
        ClientEntity client = new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setFirstName("F")
                .setBotType(BotType.TELEGRAM)
                .setInWhitelist(true);
        client = clientRepository.save(client);

        if (withSubscription) {
            SubscriptionEntity subscription =
                    new SubscriptionEntity().setClient(client).setExpiresAt(expiresAt);
            subscriptionRepository.save(subscription);
        }

        return chatId;
    }

    /**
     * {@link UpdateBuilder#withChatId(String)} парсит переданную строку как {@code Long} (так
     * же, как это делает настоящий Telegram Update), поэтому тестовый chatId обязан быть
     * числовым - в отличие от фикстур ("2".."8"), здесь он должен быть заведомо уникальным.
     */
    private String randomNumericChatId() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000, Long.MAX_VALUE));
    }

    private BotApiMethod<?> sendText(String chatId, String text) {
        Update update = new UpdateBuilder().withChatId(chatId).withText(text).build();
        return telegramBot.onWebhookUpdateReceived(update);
    }

    private BotApiMethod<?> sendCallback(String chatId, String buttonText) {
        Update update =
                new UpdateBuilder().withChatId(chatId).withText(buttonText).build(BotDataType.CALLBACK);
        return telegramBot.onWebhookUpdateReceived(update);
    }

    private String text(BotApiMethod<?> apiMethod) {
        return ((SendMessage) apiMethod).getText();
    }
}
