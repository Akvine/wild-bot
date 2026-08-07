package ru.akvine.wild.bot.bot.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.services.integration.max.dto.Button;

@Data
@Accessors(chain = true)
public class InlineKeyboard {
    private InlineKeyboardMarkup telegramKeyboard;
    private Button[] maxButtons;

    public InlineKeyboard(InlineKeyboardMarkup telegramKeyboard) {
        this.telegramKeyboard = telegramKeyboard;
    }

    public InlineKeyboard(Button[] maxButtons) {
        this.maxButtons = maxButtons;
    }
}
