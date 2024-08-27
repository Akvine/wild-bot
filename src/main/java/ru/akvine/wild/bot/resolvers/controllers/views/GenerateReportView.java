package ru.akvine.wild.bot.resolvers.controllers.views;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.START_GENERATION_BUTTON_TEXT;

@Component
public class GenerateReportView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton startGenerationButton = new InlineKeyboardButton();
        startGenerationButton.setText(START_GENERATION_BUTTON_TEXT);
        startGenerationButton.setCallbackData(START_GENERATION_BUTTON_TEXT);

        InlineKeyboardButton backButton = KeyboardFactory.getBackButton();
        return KeyboardFactory.createVerticalKeyboard(startGenerationButton, backButton);
    }

    @Override
    public String getMessage(String chatId) {
        return "Бот сгенерирует отчёт в формате Excel по всем  проведенным тестам";
    }

    @Override
    public ClientState byState() {
        return ClientState.GENERATE_REPORT_MENU;
    }
}
