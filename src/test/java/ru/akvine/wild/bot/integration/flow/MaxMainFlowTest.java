package ru.akvine.wild.bot.integration.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.akvine.wild.bot.constants.telegram.BotMessageErrorConstants.CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.integration.base.MaxBaseTest;
import ru.akvine.wild.bot.integration.base.MaxUpdateBuilder;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.services.encryption.EncryptionService;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.utils.UUIDGenerator;

/**
 * Тот же основной флоу, что и {@link TelegramMainFlowTest}, но через MAX: он идет через один
 * и тот же фильтр-пайплайн/диспетчер/резолверы состояний (см. {@code MessageDispatcherImpl}),
 * поэтому здесь проверяется главным образом, что MAX-конвертер (callback-payload вместо текста
 * кнопки, отдельный {@code SendMessageRequest}) не ломает общую логику.
 */
@ExtendWith(SpringExtension.class)
@DisplayName("Max main flow tests")
class MaxMainFlowTest extends MaxBaseTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private StateStorage<String, List<ClientState>> stateStorage;

    @Autowired
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        builder = new MaxUpdateBuilder();
    }

    @Test
    @DisplayName("/start opens the main menu and remembers MAIN_MENU state")
    void start_opensMainMenu() {
        String chatId = seedWhitelistedClient();

        SendMessageRequest response = sendText(chatId, "/start");

        assertThat(response).isNotNull();
        assertThat(stateStorage.getCurrent(chatId, BotType.MAX)).isEqualTo(ClientState.MAIN_MENU);
    }

    @Test
    @DisplayName("Main menu: 'Мои тесты' without a subscription returns the no-subscription message")
    void mainMenu_testsButton_withoutSubscription_returnsNoSubscriptionMessage() {
        String chatId = seedWhitelistedClient();
        sendText(chatId, "/start");

        SendMessageRequest response = sendCallback(chatId, TESTS_MENU);

        assertThat(response.getText()).isEqualTo(CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE);
        assertThat(stateStorage.getCurrent(chatId, BotType.MAX)).isEqualTo(ClientState.MAIN_MENU);
    }

    @Test
    @DisplayName("WB token: revoke then enter a valid token - persisted encrypted and confirmed")
    void wildberriesToken_revokeThenInput_updatesEncryptedTokenAndConfirms() {
        String chatId = seedWhitelistedClient();
        sendText(chatId, "/start");
        sendCallback(chatId, WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);
        sendCallback(chatId, REVOKE_TOKEN_BUTTON_TEXT);
        assertThat(stateStorage.getCurrent(chatId, BotType.MAX))
                .isEqualTo(ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU);

        String newToken = "AABBCCDD00112233AABBCCDD00112233";
        sendText(chatId, newToken);

        // "Токен успешно обновлен!" уходит отдельным сообщением через BotIntegrationAdapter,
        // а не в ответе на само обновление (там - сообщение меню, в которое произошёл откат).
        verify(maxIntegrationService).sendMessage(chatId, new SendMessageRequest().setText("Токен успешно обновлен!"));
        assertThat(stateStorage.getCurrent(chatId, BotType.MAX))
                .isEqualTo(ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);

        ClientEntity persisted =
                clientRepository.findByChatIdAndBotType(chatId, BotType.MAX).orElseThrow();
        assertThat(encryptionService.decrypt(persisted.getToken())).isEqualTo(newToken);
    }

    @Test
    @DisplayName("Back button returns to the previous menu")
    void backButton_returnsToPreviousState() {
        String chatId = seedWhitelistedClient();
        sendText(chatId, "/start");
        sendCallback(chatId, WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);

        sendCallback(chatId, BACK_BUTTON_TEXT);

        assertThat(stateStorage.getCurrent(chatId, BotType.MAX)).isEqualTo(ClientState.MAIN_MENU);
    }

    private String seedWhitelistedClient() {
        String chatId = "max-" + UUID.randomUUID();
        ClientEntity client = new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setFirstName("F")
                .setBotType(BotType.MAX)
                .setInWhitelist(true);
        clientRepository.save(client);
        return chatId;
    }

    private SendMessageRequest sendText(String chatId, String text) {
        Update update = builder.withChatId(chatId).withText(text).build();
        when(maxIntegrationService.getMessages(eq(chatId))).thenReturn(new Message[] {builder.buildIncomingMessage()});
        return maxBot.onUpdateReceived(new Update[] {update});
    }

    private SendMessageRequest sendCallback(String chatId, String payload) {
        Update update = builder.withChatId(chatId).withText(payload).build(BotDataType.CALLBACK);
        when(maxIntegrationService.getMessages(eq(chatId))).thenReturn(new Message[] {builder.buildIncomingMessage()});
        return maxBot.onUpdateReceived(new Update[] {update});
    }
}
