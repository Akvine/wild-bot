package ru.akvine.marketspace.bot.telegram;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@UtilityClass
public class KeyboardFactory {

    public ReplyKeyboardMarkup getYesAndNoKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Изменить"));
        row.add(new KeyboardButton("Не изменять"));

        return getKeyboard(List.of(row));
    }

    private ReplyKeyboardMarkup getKeyboard(List<KeyboardRow> rows) {

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        return replyKeyboardMarkup;
    }
}
