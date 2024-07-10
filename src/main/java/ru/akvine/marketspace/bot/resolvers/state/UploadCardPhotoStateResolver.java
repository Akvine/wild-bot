package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.akvine.marketspace.bot.controller.AdvertStartController;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.TelegramData;
import ru.akvine.marketspace.bot.utils.TelegramPhotoResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadCardPhotoStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;
    private final AdvertStartController advertStartController;
    private final TelegramPhotoResolver telegramPhotoResolver;
    private final TelegramIntegrationService telegramIntegrationService;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());

        if (data.getData().getMessage().getPhoto() == null) {
            return new SendMessage(chatId, "Загрузите фотографию карточки для теста РК или\n" +
                    "введите команду /cancel для отмены запуска");
        }

        PhotoSize photoSize = telegramPhotoResolver.resolve(data.getData().getMessage().getPhoto());
        byte[] photo = telegramIntegrationService.downloadPhoto(photoSize.getFileId(), chatId);
        sessionStorage.get(chatId).setUploadedCardPhoto(photo);
        SendMessage response = advertStartController.startAdvert(chatId);
        stateStorage.removeState(chatId);
        sessionStorage.close(chatId);
        return response;
    }

    @Override
    public ClientState getState() {
        return ClientState.UPLOAD_NEW_CARD_PHOTO_STATE;
    }
}
