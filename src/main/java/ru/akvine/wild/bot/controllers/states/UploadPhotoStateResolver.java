package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.helpers.TelegramPhotoHelper;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.validator.PhotoValidator;

@State
@Slf4j
public class UploadPhotoStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final TelegramPhotoHelper telegramPhotoHelper;
    private final TelegramIntegrationService telegramIntegrationService;
    private final MaxIntegrationService maxIntegrationService;
    private final PhotoValidator photoValidator;

    @Autowired
    public UploadPhotoStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            SessionStorage<String, ClientSessionData> sessionStorage,
            TelegramPhotoHelper telegramPhotoHelper,
            TelegramIntegrationService telegramIntegrationService,
            MaxIntegrationService maxIntegrationService,
            PhotoValidator photoValidator) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
        this.telegramPhotoHelper = telegramPhotoHelper;
        this.telegramIntegrationService = telegramIntegrationService;
        this.maxIntegrationService = maxIntegrationService;
        this.photoValidator = photoValidator;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();

        logger.info("[{}] state resolved", getState());

        Response response = new Response(chatId, botType);
        byte[] photo;
        if (botType == BotType.TELEGRAM) {
            if (payload.getMessage().getTelegramPhoto() == null) {
                return response.setTelegramResponse(new SendMessage(chatId, "Необходимо загрузить фотографию!"));
            }

            PhotoSize photoSize =
                    telegramPhotoHelper.resolve(payload.getMessage().getTelegramPhoto());
            photo = telegramIntegrationService.downloadPhoto(photoSize.getFileId(), chatId);
            photoValidator.validate(photo);
        } else {
            String photoUrl =
                    payload.getMessage() != null ? payload.getMessage().getMaxPhotoUrl() : null;
            if (photoUrl == null) {
                return response.setMaxSendMessage(
                        new MaxSendMessage().setChatId(chatId).setText("Необходимо загрузить фотографию!"));
            }

            photo = maxIntegrationService.downloadAttachment(photoUrl);
            photoValidator.validate(photo);
        }

        ClientSessionData session = sessionStorage.get(chatId);
        session.setUploadedCardPhoto(photo);
        sessionStorage.save(session);

        return setNextState(chatId, ClientState.IS_CHANGE_PRICE_MENU, botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.UPLOAD_PHOTO_MENU;
    }
}
