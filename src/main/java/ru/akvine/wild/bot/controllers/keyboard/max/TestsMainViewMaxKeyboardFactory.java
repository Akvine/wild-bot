package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.DETAIL_TEST_INFORMATION_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.GENERATE_REPORT_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.LIST_STARTED_TESTS_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.START_TEST_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class TestsMainViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button startTestButton = MaxKeyboardFactory.callbackButton(START_TEST_BUTTON_TEXT);
        Button listStartedTestsButton = MaxKeyboardFactory.callbackButton(LIST_STARTED_TESTS_BUTTON_TEXT);
        Button fillAdvertisingAccountButton = MaxKeyboardFactory.callbackButton(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT);
        Button generateReportButton = MaxKeyboardFactory.callbackButton(GENERATE_REPORT_BUTTON_TEXT);
        Button detailTestInfoButton = MaxKeyboardFactory.callbackButton(DETAIL_TEST_INFORMATION_BUTTON_TEXT);
        Button backButton = MaxKeyboardFactory.getBackButton();

        Button[][] keyboard = MaxKeyboardFactory.createVerticalKeyboard(
                startTestButton,
                listStartedTestsButton,
                fillAdvertisingAccountButton,
                generateReportButton,
                detailTestInfoButton,
                backButton);

        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.TESTS_MENU;
    }
}
