package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class AdvertsTestsAndCardsTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId, BotType botType) {
        InlineKeyboardButton listStartedTestsButton = new InlineKeyboardButton();
        listStartedTestsButton.setText(LIST_STARTED_TESTS_BUTTON_TEXT);
        listStartedTestsButton.setCallbackData(LIST_STARTED_TESTS_BUTTON_TEXT);

        InlineKeyboardButton listAdvertsButton = new InlineKeyboardButton();
        listStartedTestsButton.setText(LIST_ADVERTS_BUTTON_TEXT);
        listStartedTestsButton.setCallbackData(LIST_ADVERTS_BUTTON_TEXT);

        InlineKeyboardButton listCardsButton = new InlineKeyboardButton();
        listStartedTestsButton.setText(LIST_CARDS_BUTTON_TEXT);
        listStartedTestsButton.setCallbackData(LIST_CARDS_BUTTON_TEXT);

        InlineKeyboardButton backButton = TelegramKeyboardFactory.getBackButton();

        InlineKeyboardMarkup markup = TelegramKeyboardFactory.createVerticalKeyboard(
                listStartedTestsButton, listAdvertsButton, listCardsButton, backButton);

        return new InlineKeyboard(markup);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.ADVERTS_TESTS_CARDS_MENU;
    }
}
