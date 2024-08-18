package ru.akvine.marketspace.bot.resolvers.controllers.views;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.*;

@Component
public class TestsMainView implements TelegramView {
    private final static String NEW_LINE = "\n";

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton startTestButton = new InlineKeyboardButton();
        startTestButton.setText(START_TEST_BUTTON_TEXT);
        startTestButton.setCallbackData(START_TEST_BUTTON_TEXT);

        InlineKeyboardButton listStartedTestsButton = new InlineKeyboardButton();
        listStartedTestsButton.setText(LIST_STARTED_TESTS_BUTTON_TEXT);
        listStartedTestsButton.setCallbackData(LIST_STARTED_TESTS_BUTTON_TEXT);

        InlineKeyboardButton fillAdvertisingAccountButton = new InlineKeyboardButton();
        fillAdvertisingAccountButton.setText(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT);
        fillAdvertisingAccountButton.setCallbackData(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT);

        InlineKeyboardButton generateReportButton = new InlineKeyboardButton();
        generateReportButton.setText(GENERATE_REPORT_BUTTON_TEXT);
        generateReportButton.setCallbackData(GENERATE_REPORT_BUTTON_TEXT);

        InlineKeyboardButton detailTestInfoButton = new InlineKeyboardButton();
        detailTestInfoButton.setText(DETAIL_TEST_INFORMATION_BUTTON_TEXT);
        detailTestInfoButton.setCallbackData(DETAIL_TEST_INFORMATION_BUTTON_TEXT);

        InlineKeyboardButton backButton = KeyboardFactory.getBackButton();

        return KeyboardFactory.createVerticalKeyboard(
                startTestButton,
                listStartedTestsButton,
                fillAdvertisingAccountButton,
                generateReportButton,
                detailTestInfoButton,
                backButton);
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("<b>").append(START_TEST_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Выберите категорию товара, и бот автоматически создаст").append(NEW_LINE)
                .append("новую рекламную кампанию или запустит уже").append(NEW_LINE)
                .append("созданную").append(NEW_LINE)
                .append("<b>").append(LIST_STARTED_TESTS_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Запросите вывод списка запущенных тестов").append(NEW_LINE)
                .append("<b>").append(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Запросите QR-код для пополнения").append(NEW_LINE)
                .append("рекламного кабинета, и бот выдаст его для").append(NEW_LINE)
                .append("оплаты, пополнит счётчик тестов. ").append(NEW_LINE)
                .append("<b>").append(GENERATE_REPORT_BUTTON_TEXT).append("</b>").append(": ").append(NEW_LINE)
                .append("Бот сгенерирует отчёт в формате Excel").append(NEW_LINE)
                .append("по всем проведенным тестам").append(NEW_LINE)
                .append("<b>").append(DETAIL_TEST_INFORMATION_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Вы можете через id теста посмотреть детальную информацию");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.TESTS_MENU;
    }
}
