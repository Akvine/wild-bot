package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.MALE_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class ChooseTypeViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button maleButton = MaxComponentsFactory.callbackButton(MALE_BUTTON_TEXT);
        Button femaleButton = MaxComponentsFactory.callbackButton(FEMALE_BUTTON_TEXT);

        Button[][] keyboard = MaxComponentsFactory.createVerticalKeyboard(
                maleButton, femaleButton, MaxComponentsFactory.getBackButton());
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
