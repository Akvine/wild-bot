package ru.akvine.wild.bot.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants;
import ru.akvine.wild.bot.enums.TelegramDataType;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.integration.base.UpdateBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;
import static ru.akvine.wild.bot.constants.telegram.TelegramMessageErrorConstants.CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE;
import static ru.akvine.wild.bot.constants.telegram.TelegramMessageErrorConstants.CLIENT_NOT_IN_WHITELIST_MESSAGE;
import static ru.akvine.wild.bot.integration.config.TestConstants.*;

@ExtendWith(SpringExtension.class)
@DisplayName("Bot workflow flow tests")
public class WorkflowTest extends BaseTest {
    @Test
    @DisplayName("Client has no subscription")
    public void client_has_no_subscription() {
        builder = new UpdateBuilder();
        builder.withFirstname("FirstName");
        builder.withLastname("SecondName");
        builder.withText("some text");
        builder.withUsername("some username");
        builder.withChatId("1");

        Update update = builder.build();
        BotApiMethod<?> apiMethod = messageFilter.handle(update);
        SendMessage message = (SendMessage) apiMethod;

        String text = message.getText();
        String chatId = message.getChatId();

        assertThat(text).isNotNull();
        assertThat(text).isEqualTo(CLIENT_NOT_IN_WHITELIST_MESSAGE);
        assertThat(chatId).isNotNull();
        assertThat(chatId).isEqualTo("1");
    }

    @Test
    @DisplayName("Client has subscription")
    public void client_has_subscription() {
        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_2);

        Update update = builder.build();
        BotApiMethod<?> apiMethod = messageFilter.handle(update);
        SendMessage message = (SendMessage) apiMethod;

        String text = message.getText();
        String chatId = message.getChatId();

        assertThat(text).isNotNull();
        assertThat(text).isNotEqualTo(CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE);
        assertThat(chatId).isNotNull();
        assertThat(chatId).isEqualTo(CHAT_ID_2);
    }

    @Test
    @DisplayName("Generate report")
    public void generate_report() {
        String expectedMessage = "Ваш отчёт готов, вы можете перейти по команде назад для запуска ещё одного теста";

        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_3);

        Update update = builder.build();
        messageFilter.handle(update);

        Update callbackUpdate = builder
                .withChatId(CHAT_ID_3)
                .withText(TelegramButtonConstants.TESTS_MENU)
                .build(TelegramDataType.CALLBACK);

        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withChatId(CHAT_ID_3)
                .withText(GENERATE_REPORT_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withChatId(CHAT_ID_3)
                .withText(START_GENERATION_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);

        SendMessage successfulGenerateReportMessage =
                (SendMessage) messageFilter.handle(callbackUpdate);

        String text = successfulGenerateReportMessage.getText();
        String chatId = successfulGenerateReportMessage.getChatId();

        assertThat(text).isNotBlank();
        assertThat(chatId).isNotBlank();
        assertThat(chatId).isEqualTo(CHAT_ID_3);
        assertThat(text).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Fill advertising account")
    public void fill_advertising_account() {
        String expectedMessage = "Спасибо! В ближайшее время бот отправит вам QR-код на пополнение бюджета :-)";

        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_4);

        Update update = builder.build();
        messageFilter.handle(update);

        Update callbackUpdate = builder
                .withChatId(CHAT_ID_4)
                .withText(TelegramButtonConstants.TESTS_MENU)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withChatId(CHAT_ID_4)
                .withText(TelegramButtonConstants.FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withChatId(CHAT_ID_4)
                .withText(QUERY_QR_CODE_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        SendMessage fillAccountMessage = (SendMessage) messageFilter.handle(callbackUpdate);

        String text = fillAccountMessage.getText();
        String chatId = fillAccountMessage.getChatId();

        assertThat(text).isNotBlank();
        assertThat(chatId).isNotBlank();
        assertThat(chatId).isEqualTo(CHAT_ID_4);
        assertThat(text).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Get detail info by test")
    public void get_detail_info_by_test() {
        String expectedMessage = "1. ID: -1\n" +
                "2. Просмотры: 1\n" +
                "3. Клики: 1\n" +
                "4. Показатель кликабельности (ctr): 1\n" +
                "5. Затраты: 1\n" +
                "6. Средняя стоимость клика (cpc): 1\n" +
                "7. Количество добавлений товаров в корзину (atbs): 1\n" +
                "8. Количество заказов: (orders): 1\n" +
                "9. Отношение количества заказов к общему количеству посещений кампании (cr): 1\n" +
                "10. Количество заказанных товаров (shks): 1\n" +
                "11. Заказов на сумму (sum_price): 1\n" +
                "12. Идентификатор РК (Advert ID): -1\n" +
                "13. Время запуска теста: 2024-01-04T00:00";
        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_5);

        Update update = builder.build();
        messageFilter.handle(update);

        Update callbackUpdate = builder
                .withChatId(CHAT_ID_5)
                .withText(TelegramButtonConstants.TESTS_MENU)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withChatId(CHAT_ID_5)
                .withText(TelegramButtonConstants.DETAIL_TEST_INFORMATION_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        update = builder
                .withChatId(CHAT_ID_5)
                .withText("-1")
                .build();
        SendMessage getDetailInfoMessage = (SendMessage) messageFilter.handle(update);

        String text = getDetailInfoMessage.getText();
        String chatId = getDetailInfoMessage.getChatId();

        assertThat(text).isNotBlank();
        assertThat(chatId).isNotBlank();
        assertThat(chatId).isEqualTo(CHAT_ID_5);
        assertThat(text).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Get instructions")
    public void get_instructions() {
        String expectedMessage = "<b>Запуск рекламной кампании</b> \uD83D\uDE80: выберите\n" +
                "категорию товара, и бот автоматически\n" +
                "создаст новую рекламную кампанию или\n" +
                "запустит уже cозданную\n" +
                "<b>Управление ставками CPM</b> \uD83D\uDCB0: бот будет\n" +
                "автоматически регулировать ставки для\n" +
                "оптимального размещения рекламы.\n" +
                "<b>Настройка цен и скидок</b> \uD83C\uDFF7: введите\n" +
                "желаемую цену на товар, и бот рассчитает\n" +
                "необходимую скидку.\n" +
                "<b>Пополнение рекламного кабинета</b> \uD83D\uDCF2:\n" +
                "запросите QR-код для пополнения, и бот\n" +
                "выдаст его для оплаты.\n" +
                "<b>Оптимальное размещение рекламы</b> \uD83D\uDCC8:\n" +
                "бот обеспечит лучшее размещение ваших  объявлений";

        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_6);

        Update update = builder.build();
        messageFilter.handle(update);

        Update callbackUpdate = builder
                .withChatId(CHAT_ID_6)
                .withText(TelegramButtonConstants.INSTRUCTIONS_FOR_USE_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        SendMessage instructionsMessage = (SendMessage) messageFilter.handle(callbackUpdate);

        String text = instructionsMessage.getText();
        String chatId = instructionsMessage.getChatId();

        assertThat(text).isNotBlank();
        assertThat(chatId).isNotBlank();
        assertThat(chatId).isEqualTo(CHAT_ID_6);
        assertThat(text).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Payment subscription")
    public void payment_subscription() {
        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_7);
        Update update = builder.build();
        messageFilter.handle(update);

        Update callbackUpdate = builder
                .withChatId(CHAT_ID_7)
                .withText(TelegramButtonConstants.ADD_SUBSCRIPTION_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withChatId(CHAT_ID_7)
                .withText(TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        SendMessage subscriptionMessage = (SendMessage) messageFilter.handle(callbackUpdate);

        String text = subscriptionMessage.getText();
        String chatId = subscriptionMessage.getChatId();

        assertThat(text).isNotBlank();
        assertThat(chatId).isNotBlank();
        assertThat(chatId).isEqualTo(CHAT_ID_7);
    }

    @Test
    @DisplayName("Start test - without changing price")
    public void start_test() {
        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_8);
        Update update = builder.build();
        messageFilter.handle(update);

        Update callbackUpdate = builder
                .withText(TelegramButtonConstants.TESTS_MENU)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withText(TelegramButtonConstants.START_TEST_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        callbackUpdate = builder
                .withText(TelegramButtonConstants.MALE_BUTTON_TEXT)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);

        String categoryId = "2";
        callbackUpdate = builder
                .withText(categoryId)
                .build(TelegramDataType.CALLBACK);
        messageFilter.handle(callbackUpdate);


    }

}
