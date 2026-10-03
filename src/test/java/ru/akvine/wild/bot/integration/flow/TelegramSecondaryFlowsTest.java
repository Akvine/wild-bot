package ru.akvine.wild.bot.integration.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.AdvertType;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.integration.base.TelegramBaseTest;
import ru.akvine.wild.bot.integration.base.UpdateBuilder;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.utils.UUIDGenerator;

/**
 * Остальные экраны бота: списки кампаний и карточек, отчёт, детальная статистика, QR-код, подписка, инструкция и
 * настройки аккаунта Wildberries.
 */
@DisplayName("Telegram: списки, отчёты, QR-код, подписка и настройки")
class TelegramSecondaryFlowsTest extends TelegramBaseTest {
    private static final AtomicInteger IDS = new AtomicInteger(7_000_000);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private AdvertRepository advertRepository;

    @Autowired
    private AdvertStatisticRepository statisticRepository;

    @Autowired
    private StateStorage<String, List<ClientState>> stateStorage;

    private String chatId;
    private ClientEntity client;
    private AdvertEntity advert;
    private AdvertStatisticEntity statistic;

    @BeforeEach
    void setUp() {
        builder = new UpdateBuilder();
        chatId = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE));
        client = clientRepository.save(new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setFirstName("Tester")
                .setBotType(BotType.TELEGRAM)
                .setInWhitelist(true)
                .setToken("AABBCCDD00112233AABBCCDD00112233"));
        client.increaseAvailableTestsCount(2);
        client = clientRepository.save(client);
        subscriptionRepository.save(new SubscriptionEntity()
                .setClient(client)
                .setExpiresAt(LocalDateTime.now().plusDays(30)));

        CardEntity card = cardRepository.save(new CardEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(IDS.incrementAndGet())
                .setExternalTitle("Куртка зимняя")
                .setCategoryId(IDS.incrementAndGet())
                .setCategoryTitle("Верхняя одежда")
                .setBarcode("barcode-" + chatId)
                .setOwnerClient(client)
                .setCardType(cardTypeRepository.findByType(MALE_BUTTON_TEXT).orElseThrow()));
        advert = advertRepository.save(new AdvertEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(IDS.incrementAndGet())
                .setExternalTitle("Campaign")
                .setChangeTime(new Date())
                .setStatus(AdvertStatus.RUNNING)
                .setOrdinalStatus(AdvertStatus.RUNNING.getCode())
                .setType(AdvertType.AUTO)
                .setOrdinalType(AdvertType.AUTO.getCode())
                .setCpm(150)
                .setStartBudgetSum(1100)
                .setCheckBudgetSum(900)
                .setStartCheckDateTime(LocalDateTime.now().minusHours(2))
                .setNextCheckDateTime(LocalDateTime.now().plusMinutes(5))
                .setCard(card));
        statistic = statisticRepository.save(new AdvertStatisticEntity()
                .setViews("100")
                .setClicks("10")
                .setCtr("10.0")
                .setCpc("1.5")
                .setSum("15")
                .setAtbs("1")
                .setOrders("2")
                .setCr("20")
                .setShks("2")
                .setSumPrice("300")
                .setPhoto(new byte[] {1, 2, 3})
                .setActive(false)
                .setAdvertEntity(advert)
                .setClient(client));
    }

    private BotApiMethod<?> text(String text) {
        return telegramBot.onWebhookUpdateReceived(
                new UpdateBuilder().withChatId(chatId).withText(text).build());
    }

    private BotApiMethod<?> button(String text) {
        return telegramBot.onWebhookUpdateReceived(
                new UpdateBuilder().withChatId(chatId).withText(text).build(BotDataType.CALLBACK));
    }

    private ClientState state() {
        return stateStorage.getCurrent(chatId, BotType.TELEGRAM);
    }

    private static String messageOf(BotApiMethod<?> method) {
        return ((SendMessage) method).getText();
    }

    private void openTestsMenu() {
        text("/start");
        button(TESTS_MENU);
    }

    @Test
    @DisplayName("Список запущенных тестов показывает кампанию и остаток попыток")
    void listStartedTests() {
        openTestsMenu();
        button(ADVERTS_TESTS_AND_CARDS_BUTTON_TEXT);

        String message = messageOf(button(LIST_STARTED_TESTS_BUTTON_TEXT));

        assertThat(message).contains(String.valueOf(advert.getExternalId()));
    }

    @Test
    @DisplayName("Список кампаний и карточек клиента")
    void listAdvertsAndCards() {
        openTestsMenu();
        button(ADVERTS_TESTS_AND_CARDS_BUTTON_TEXT);

        String adverts = messageOf(button(LIST_ADVERTS_BUTTON_TEXT));
        String cards = messageOf(button(LIST_CARDS_BUTTON_TEXT));

        assertThat(adverts).contains("ID: " + advert.getExternalId()).contains("RUNNING");
        assertThat(cards).contains("Куртка зимняя").contains(MALE_BUTTON_TEXT);
        assertThat(messageOf(button("непонятное"))).contains("выбрать действие");
    }

    @Test
    @DisplayName("Отчёт: Excel-файл уходит клиенту, состояние переходит на завершение отчёта")
    void generateReport() {
        openTestsMenu();
        button(GENERATE_REPORT_BUTTON_TEXT);
        assertThat(messageOf(button("непонятное"))).contains("выбрать действие");

        button(START_GENERATION_BUTTON_TEXT);

        verify(telegramIntegrationService).sendFile(eq(chatId), anyString(), any(byte[].class));
        assertThat(state()).isEqualTo(ClientState.FINISH_GENERATION_REPORT_MENU);
        assertThat(messageOf(text("что угодно"))).contains("выбрать действие");
    }

    @Test
    @DisplayName("Детальная информация: неверный ввод, неизвестный id и существующая строка отчёта")
    void detailedTestInformation() {
        openTestsMenu();
        button(DETAIL_TEST_INFORMATION_BUTTON_TEXT);

        assertThat(messageOf(text("abc"))).contains("целое число");
        assertThat(messageOf(text("999999999"))).contains("В отчете нет строки");

        String message = messageOf(text(String.valueOf(statistic.getId())));

        assertThat(message)
                .contains("ID: " + statistic.getId())
                .contains("Просмотры: 100")
                .contains("Advert ID): " + advert.getExternalId());
        verify(telegramIntegrationService).sendImage(eq(chatId), any(byte[].class), anyString());
    }

    @Test
    @DisplayName("Пополнение рекламного кабинета: QR-код генерируется внутренним сервисом и отправляется картинкой")
    void qrCodeForFillingAdvertisingAccount() {
        openTestsMenu();
        button(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT);
        assertThat(messageOf(button("непонятное"))).contains("выбрать действие");

        String message = messageOf(button(QUERY_QR_CODE_BUTTON_TEXT));

        assertThat(message).contains("QR-код");
        verify(telegramIntegrationService).sendImage(eq(chatId), any(byte[].class), anyString());
    }

    @Test
    @DisplayName("Подписка: оплата продлевает срок только когда прежняя подписка закончилась")
    void subscriptionPayment() {
        text("/start");
        button(ADD_SUBSCRIPTION_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.SUBSCRIBE_MENU);

        assertThat(messageOf(button("непонятное"))).contains("выбрать действие");
        String whileActive = messageOf(button(PAY_SUBSCRIPTION_BUTTON_TEXT));
        assertThat(whileActive).contains("еще активна");

        SubscriptionEntity subscription =
                subscriptionRepository.findByChatId(chatId).orElseThrow();
        subscription.setExpiresAt(LocalDateTime.now().minusDays(1));
        subscriptionRepository.save(subscription);

        String afterExpiry = messageOf(button(PAY_SUBSCRIPTION_BUTTON_TEXT));
        assertThat(afterExpiry).contains("Платеж прошел успешно");
        assertThat(subscriptionRepository.findByChatId(chatId).orElseThrow().getExpiresAt())
                .isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Инструкция открывается из главного меню; неожиданный ввод - подсказка")
    void instruction() {
        text("/start");
        button(INSTRUCTIONS_FOR_USE_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.INSTRUCTIONS_MENU);

        assertThat(messageOf(text("что-то"))).contains("выбрать действие");

        button(BACK_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.MAIN_MENU);
    }

    @Test
    @DisplayName("Настройки аккаунта: отзыв токена очищает токен и переводит на ввод нового")
    void accountSettingsRevokeToken() {
        text("/start");
        button(WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);
        assertThat(messageOf(button("непонятное"))).contains("выбрать действие");

        button(REVOKE_TOKEN_BUTTON_TEXT);

        assertThat(state()).isEqualTo(ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU);
        assertThat(clientRepository
                        .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                        .orElseThrow()
                        .getToken())
                .isNull();
    }

    @Test
    @Disabled("Известный дефект: кнопка «Сменить ID склада» ведёт в CHANGE_WAREHOUSE_ID_MENU, для которого нет "
            + "ни StateResolver, ни BotView - клиент застревает, «Назад» тоже падает. Функцию нужно либо "
            + "реализовать, либо убрать кнопку; после этого включить тест.")
    @DisplayName("Настройки аккаунта: смена ID склада открывает экран и позволяет вернуться")
    void accountSettingsChangeWarehouse() {
        text("/start");
        button(WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT);

        button(CHANGE_WAREHOUSE_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.CHANGE_WAREHOUSE_ID_MENU);

        button(BACK_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);
    }
}
