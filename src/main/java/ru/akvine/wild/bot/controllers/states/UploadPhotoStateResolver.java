package ru.akvine.wild.bot.controllers.states;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.helpers.TelegramPhotoHelper;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;
import ru.akvine.wild.bot.validator.PhotoValidator;

import java.util.List;

@State
@Slf4j
public class UploadPhotoStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final TelegramPhotoHelper telegramPhotoHelper;
    private final TelegramIntegrationService telegramIntegrationService;
    private final PhotoValidator photoValidator;

    @Autowired
    public UploadPhotoStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                    TelegramViewFacade viewFacade,
                                    TelegramDataResolverFacade dataResolverFacade,
                                    SessionStorage<String, ClientSessionData> sessionStorage,
                                    TelegramPhotoHelper telegramPhotoHelper,
                                    TelegramIntegrationService telegramIntegrationService,
                                    PhotoValidator photoValidator) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
        this.telegramPhotoHelper = telegramPhotoHelper;
        this.telegramIntegrationService = telegramIntegrationService;
        this.photoValidator = photoValidator;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        logger.info("[{}] state resolved", getState());

        if (telegramData.getData().getMessage().getPhoto() == null) {
            return new SendMessage(chatId, "Необходимо загрузить фотографию!");
        }

        PhotoSize photoSize = telegramPhotoHelper.resolve(telegramData.getData().getMessage().getPhoto());
        byte[] photo = telegramIntegrationService.downloadPhoto(photoSize.getFileId(), chatId);
        photoValidator.validate(photo);

        ClientSessionData session = sessionStorage.get(chatId);
        session.setUploadedCardPhoto(photo);
        sessionStorage.save(session);

        return setNextState(chatId, ClientState.IS_CHANGE_PRICE_MENU);
    }

    @Override
    public ClientState getState() {
        return ClientState.UPLOAD_PHOTO_MENU;
    }
}
