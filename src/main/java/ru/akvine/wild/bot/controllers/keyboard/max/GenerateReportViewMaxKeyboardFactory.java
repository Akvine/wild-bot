package ru.akvine.wild.bot.controllers.keyboard.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.START_GENERATION_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Component
public class GenerateReportViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button startGenerationButton = MaxComponentsFactory.callbackButton(START_GENERATION_BUTTON_TEXT);
        Button backButton = MaxComponentsFactory.getBackButton();

        Button[][] keyboard = MaxComponentsFactory.createVerticalKeyboard(startGenerationButton, backButton);
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.GENERATE_REPORT_MENU;
    }
}
