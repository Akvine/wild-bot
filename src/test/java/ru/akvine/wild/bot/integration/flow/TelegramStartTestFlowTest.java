package ru.akvine.wild.bot.integration.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.entities.AdvertEntity;
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
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertBudgetInfoResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsData;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GoodDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GoodSizeDto;
import ru.akvine.wild.bot.utils.UUIDGenerator;

/**
 * Основной сценарий продукта: клиент запускает тест рекламной кампании через бота. Реальны фильтры, диспетчер,
 * резолверы состояний, вьюхи, сервисы, хранилища и БД (H2); замоканы только внешние границы - Wildberries и Telegram.
 */
@DisplayName("Telegram: запуск теста рекламной кампании")
class TelegramStartTestFlowTest extends TelegramBaseTest {
    private static final AtomicInteger IDS = new AtomicInteger(5_000_000);

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
    private StateStorage<String, List<ClientState>> stateStorage;

    private String chatId;
    private int categoryId;
    private AdvertEntity advert;

    @BeforeEach
    void setUp() throws Exception {
        builder = new UpdateBuilder();
        chatId = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE));
        categoryId = IDS.incrementAndGet();

        ClientEntity client = clientRepository.save(new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setFirstName("Tester")
                .setBotType(BotType.TELEGRAM)
                .setInWhitelist(true)
                .setToken("AABBCCDD00112233AABBCCDD00112233"));
        client.increaseAvailableTestsCount(3);
        clientRepository.save(client);
        subscriptionRepository.save(new SubscriptionEntity()
                .setClient(client)
                .setExpiresAt(LocalDateTime.now().plusDays(30)));

        CardEntity card = cardRepository.save(new CardEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(IDS.incrementAndGet())
                .setExternalTitle("Куртка")
                .setCategoryId(categoryId)
                .setCategoryTitle("Верхняя одежда")
                .setBarcode("barcode-" + categoryId)
                .setOwnerClient(client)
                .setCardType(cardTypeRepository.findByType(MALE_BUTTON_TEXT).orElseThrow()));
        advert = advertRepository.save(new AdvertEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(IDS.incrementAndGet())
                .setExternalTitle("Campaign")
                .setChangeTime(new Date())
                .setStatus(AdvertStatus.PAUSE)
                .setOrdinalStatus(AdvertStatus.PAUSE.getCode())
                .setType(AdvertType.AUTO)
                .setOrdinalType(AdvertType.AUTO.getCode())
                .setCpm(100)
                .setCard(card));

        when(telegramIntegrationService.downloadPhoto(anyString(), anyString())).thenReturn(png(800, 1000));
        GoodSizeDto size = new GoodSizeDto().setPrice(1000).setDiscountedPrice(900.0);
        GoodDto good = new GoodDto()
                .setNmId(String.valueOf(card.getExternalId()))
                .setDiscount(10)
                .setSizes(List.of(size));
        when(wildberriesIntegrationService.getGoods(any(), anyString()))
                .thenReturn(new GetGoodsResponse().setData(new GetGoodsData().setListGoods(List.of(good))));
        when(wildberriesIntegrationService.getAdvertBudgetInfo(eq(advert.getExternalId()), anyString()))
                .thenReturn(new AdvertBudgetInfoResponse().setTotal(100));
    }

    private static byte[] png(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private BotApiMethod<?> text(String text) {
        return telegramBot.onWebhookUpdateReceived(
                new UpdateBuilder().withChatId(chatId).withText(text).build());
    }

    private BotApiMethod<?> button(String text) {
        return telegramBot.onWebhookUpdateReceived(
                new UpdateBuilder().withChatId(chatId).withText(text).build(BotDataType.CALLBACK));
    }

    private BotApiMethod<?> photo() {
        Update update = new UpdateBuilder().withChatId(chatId).withText("photo").build();
        update.getMessage().setText(null);
        update.getMessage().setPhoto(List.of(new PhotoSize("file-id", "unique", 800, 1000, 100, null)));
        return telegramBot.onWebhookUpdateReceived(update);
    }

    private ClientState state() {
        return stateStorage.getCurrent(chatId, BotType.TELEGRAM);
    }

    private static String messageOf(BotApiMethod<?> method) {
        return ((SendMessage) method).getText();
    }

    private void goToPhotoUpload() {
        text("/start");
        button(TESTS_MENU);
        button(START_TEST_BUTTON_TEXT);
        button(MALE_BUTTON_TEXT);
        text(String.valueOf(categoryId));
    }

    @Test
    @DisplayName("Запуск теста без смены цены: выбор типа и категории, фото, «Оставить» - кампания RUNNING")
    void startTestKeepingPrice() {
        text("/start");
        assertThat(state()).isEqualTo(ClientState.MAIN_MENU);
        button(TESTS_MENU);
        assertThat(state()).isEqualTo(ClientState.TESTS_MENU);
        button(START_TEST_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.CHOOSE_TYPE_MENU);
        button(MALE_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.CHOOSE_CATEGORY_MENU);
        text(String.valueOf(categoryId));
        assertThat(state()).isEqualTo(ClientState.UPLOAD_PHOTO_MENU);

        BotApiMethod<?> afterPhoto = photo();
        assertThat(state()).isEqualTo(ClientState.IS_CHANGE_PRICE_MENU);
        assertThat(messageOf(afterPhoto)).contains("Цена без скидки: 1000").contains("Скидка: 10");

        BotApiMethod<?> started = button(KEEP_PRICE_BUTTON_TEXT);

        assertThat(messageOf(started)).contains("Запущена кампания").contains("Advert id = " + advert.getExternalId());
        AdvertEntity reloaded = advertRepository.findById(advert.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AdvertStatus.RUNNING);
        assertThat(reloaded.getNextCheckDateTime()).isNotNull();
        // регрессия: у только что синхронизированной кампании startBudgetSum == null, раньше это давало NPE
        assertThat(reloaded.getStartBudgetSum()).isEqualTo(1100);
        verify(wildberriesIntegrationService).startAdvert(eq(advert.getExternalId()), anyString());
        verify(wildberriesIntegrationService).advertBudgetDeposit(eq(advert.getExternalId()), eq(1000), anyString());
        verify(wildberriesIntegrationService).uploadPhoto(any(), anyString());
        verify(wildberriesIntegrationService, never()).setGoodPriceAndDiscount(any(), anyString());
        assertThat(state()).isEqualTo(ClientState.TESTS_MENU);
    }

    @Test
    @DisplayName("Запуск теста со сменой цены и скидки: числа проверяются, новая цена уходит в Wildberries")
    void startTestChangingPrice() {
        goToPhotoUpload();
        photo();
        assertThat(state()).isEqualTo(ClientState.IS_CHANGE_PRICE_MENU);

        button(CHANGE_PRICE_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.INPUT_NEW_PRICE_MENU);
        assertThat(messageOf(text("не число"))).contains("в виде числа");
        assertThat(state()).isEqualTo(ClientState.INPUT_NEW_PRICE_MENU);
        text("1500");
        assertThat(state()).isEqualTo(ClientState.INPUT_NEW_DISCOUNT_MENU);
        assertThat(messageOf(text("двадцать"))).contains("в виде числа");
        text("20");
        assertThat(state()).isEqualTo(ClientState.ACCEPT_NEW_PRICE_MENU);

        BotApiMethod<?> started = button(KEEP_PRICE_BUTTON_TEXT);

        assertThat(messageOf(started)).contains("Запущена кампания");
        verify(wildberriesIntegrationService).setGoodPriceAndDiscount(any(), anyString());
        assertThat(advertRepository.findById(advert.getId()).orElseThrow().getStatus())
                .isEqualTo(AdvertStatus.RUNNING);
    }

    @Test
    @DisplayName("Подтверждение новой цены можно отменить и ввести заново")
    void acceptPriceCanBeChanged() {
        goToPhotoUpload();
        photo();
        button(CHANGE_PRICE_BUTTON_TEXT);
        text("1500");
        text("20");

        button(CHANGE_PRICE_BUTTON_TEXT);

        assertThat(state()).isEqualTo(ClientState.INPUT_NEW_PRICE_MENU);
        assertThat(messageOf(text("???"))).contains("в виде числа");
        assertThat(messageOf(button("Что-то непонятное"))).contains("в виде числа");
    }

    @Test
    @DisplayName("Некорректные вводы на шагах выбора: тип, категория и фото")
    void invalidInputsOnSelectionSteps() {
        text("/start");
        button(TESTS_MENU);
        button(START_TEST_BUTTON_TEXT);

        assertThat(messageOf(button("Неизвестный тип"))).contains("выбрать действие");
        assertThat(state()).isEqualTo(ClientState.CHOOSE_TYPE_MENU);
        button(MALE_BUTTON_TEXT);
        assertThat(messageOf(text("не число"))).contains("выбрать действие");
        assertThat(state()).isEqualTo(ClientState.CHOOSE_CATEGORY_MENU);
        text(String.valueOf(categoryId));

        Update withoutPhoto =
                new UpdateBuilder().withChatId(chatId).withText("просто текст").build();
        assertThat(messageOf(telegramBot.onWebhookUpdateReceived(withoutPhoto))).contains("загрузить фотографию");
        assertThat(state()).isEqualTo(ClientState.UPLOAD_PHOTO_MENU);
    }

    @Test
    @DisplayName("Кнопка «Назад» возвращает на предыдущий шаг выбора")
    void backButtonWalksBack() {
        goToPhotoUpload();

        button(BACK_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.CHOOSE_CATEGORY_MENU);
        button(BACK_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.CHOOSE_TYPE_MENU);
        button(BACK_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.TESTS_MENU);
    }

    @Test
    @DisplayName("Нет доступных попыток: запуск теста отклоняется сообщением, состояние не меняется")
    void noAvailableTests() {
        ClientEntity client = clientRepository
                .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                .orElseThrow();
        client.setAvailableTestsCount(0);
        clientRepository.save(client);
        text("/start");
        button(TESTS_MENU);

        BotApiMethod<?> response = button(START_TEST_BUTTON_TEXT);

        assertThat(messageOf(response)).contains("нет доступных попыток");
        assertThat(state()).isEqualTo(ClientState.TESTS_MENU);
    }

    @Test
    @DisplayName("Остальные пункты меню тестов открывают свои экраны")
    void testsMenuNavigation() {
        text("/start");
        button(TESTS_MENU);

        button(GENERATE_REPORT_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.GENERATE_REPORT_MENU);
        button(BACK_BUTTON_TEXT);

        button(ADVERTS_TESTS_AND_CARDS_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.ADVERTS_TESTS_CARDS_MENU);
        button(BACK_BUTTON_TEXT);

        button(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.FILL_ADVERTISING_ACCOUNT_MENU);
        button(BACK_BUTTON_TEXT);

        button(DETAIL_TEST_INFORMATION_BUTTON_TEXT);
        assertThat(state()).isEqualTo(ClientState.DETAIL_TEST_INFO_MENU);
        button(BACK_BUTTON_TEXT);

        assertThat(messageOf(button("непонятное"))).contains("выбрать действие");
        assertThat(state()).isEqualTo(ClientState.TESTS_MENU);
    }
}
