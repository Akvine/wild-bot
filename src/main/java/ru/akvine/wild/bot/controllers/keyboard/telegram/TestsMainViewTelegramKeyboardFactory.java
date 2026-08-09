package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.DETAIL_TEST_INFORMATION_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.GENERATE_REPORT_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class TestsMainViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
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

        InlineKeyboardButton backButton = TelegramKeyboardFactory.getBackButton();

        InlineKeyboardMarkup markup = TelegramKeyboardFactory.createVerticalKeyboard(
                startTestButton,
                listStartedTestsButton,
                fillAdvertisingAccountButton,
                generateReportButton,
                detailTestInfoButton,
                backButton);

        return new InlineKeyboard(markup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.TESTS_MENU;
    }
}
