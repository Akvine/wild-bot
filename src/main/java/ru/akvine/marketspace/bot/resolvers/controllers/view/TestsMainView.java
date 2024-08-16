package ru.akvine.marketspace.bot.resolvers.controllers.view;

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
                .append(START_TEST_BUTTON_TEXT).append("\uD83D\uDE80: ").append(NEW_LINE)
                .append("выберите категорию товара, и бот автоматически создаст").append(NEW_LINE)
                .append("новую рекламную кампанию или запустит уже").append(NEW_LINE)
                .append("созданную").append(NEW_LINE)
                .append(LIST_STARTED_TESTS_BUTTON_TEXT).append("\uD83D\uDCB0: ").append(NEW_LINE)
                .append("запросите вывод списка запущенных тестов").append(NEW_LINE)
                .append(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT).append("\uD83D\uDCF2: ").append(NEW_LINE)
                .append("запросите QR-код для пополнения").append(NEW_LINE)
                .append("рекламного кабинета, и бот выдаст его для").append(NEW_LINE)
                .append("оплаты, пополнит счётчик тестов. ").append(NEW_LINE)
                .append(GENERATE_REPORT_BUTTON_TEXT).append("✍\uFE0F: ").append(NEW_LINE)
                .append("бот сгенерирует отчёт в формате Excel").append(NEW_LINE)
                .append("по всем проведенным тестам").append(NEW_LINE)
                .append(DETAIL_TEST_INFORMATION_BUTTON_TEXT).append("\uD83D\uDCDD: ").append(NEW_LINE)
                .append("вы можете через id теста  посмотреть детальную информацию");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.TESTS_MENU;
    }
}
