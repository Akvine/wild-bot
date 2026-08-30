package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.DETAIL_TEST_INFORMATION_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.GENERATE_REPORT_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.LIST_STARTED_TESTS_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.START_TEST_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class TestsMainViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button startTestButton = MaxComponentsFactory.callbackButton(START_TEST_BUTTON_TEXT);
        Button listStartedTestsButton = MaxComponentsFactory.callbackButton(LIST_STARTED_TESTS_BUTTON_TEXT);
        Button fillAdvertisingAccountButton = MaxComponentsFactory.callbackButton(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT);
        Button generateReportButton = MaxComponentsFactory.callbackButton(GENERATE_REPORT_BUTTON_TEXT);
        Button detailTestInfoButton = MaxComponentsFactory.callbackButton(DETAIL_TEST_INFORMATION_BUTTON_TEXT);
        Button backButton = MaxComponentsFactory.getBackButton();

        Button[][] keyboard = MaxComponentsFactory.createVerticalKeyboard(
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
