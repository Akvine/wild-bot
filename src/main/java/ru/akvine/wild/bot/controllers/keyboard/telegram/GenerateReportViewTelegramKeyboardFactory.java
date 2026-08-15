package ru.akvine.wild.bot.controllers.keyboard.telegram;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.START_GENERATION_BUTTON_TEXT;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
public class GenerateReportViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        InlineKeyboardButton startGenerationButton = new InlineKeyboardButton();
        startGenerationButton.setText(START_GENERATION_BUTTON_TEXT);
        startGenerationButton.setCallbackData(START_GENERATION_BUTTON_TEXT);

        InlineKeyboardButton backButton = TelegramKeyboardFactory.getBackButton();
        InlineKeyboardMarkup keyboard =
                TelegramKeyboardFactory.createVerticalKeyboard(startGenerationButton, backButton);
        return new InlineKeyboard(keyboard);
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.GENERATE_REPORT_MENU;
    }
}
