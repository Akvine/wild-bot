package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class UploadPhotoView extends AbstractBotView {

    public UploadPhotoView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        return "Загрузите фотографию для карточки (формат должен быть минимум 700x900): ";
    }

    @Override
    public ClientState byState() {
        return ClientState.UPLOAD_PHOTO_MENU;
    }
}
