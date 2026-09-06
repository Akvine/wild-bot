package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class AdvertsTestsAndCardsMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId, BotType botType) {
        Button listStartedTestsButton = MaxComponentsFactory.callbackButton(LIST_STARTED_TESTS_BUTTON_TEXT);
        Button listAdvertsButton = MaxComponentsFactory.callbackButton(LIST_ADVERTS_BUTTON_TEXT);
        Button listCardsButton = MaxComponentsFactory.callbackButton(LIST_CARDS_BUTTON_TEXT);
        Button backButton = MaxComponentsFactory.getBackButton();

        Button[][] keyboard = MaxComponentsFactory.createVerticalKeyboard(
                listStartedTestsButton, listAdvertsButton, listCardsButton, backButton);

        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.ADVERTS_TESTS_CARDS_MENU;
    }
}
