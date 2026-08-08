package ru.akvine.wild.bot.controllers.keyboard.max;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.START_GENERATION_BUTTON_TEXT;

@Component
public class GenerateReportViewMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        Button startGenerationButton = MaxKeyboardFactory.callbackButton(START_GENERATION_BUTTON_TEXT);
        Button backButton = MaxKeyboardFactory.getBackButton();

        Button[][] keyboard = MaxKeyboardFactory.createVerticalKeyboard(startGenerationButton, backButton);
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
