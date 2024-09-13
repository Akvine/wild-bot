package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

@View
@RequiredArgsConstructor
public class UploadPhotoView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
    }

    @Override
    public String getMessage(String chatId) {
        return "Загрузите фотографию для карточки (формат должен быть минимум 700x900): ";
    }

    @Override
    public ClientState byState() {
        return ClientState.UPLOAD_PHOTO_MENU;
    }
}
