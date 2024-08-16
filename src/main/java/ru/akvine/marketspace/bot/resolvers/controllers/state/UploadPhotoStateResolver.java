package ru.akvine.marketspace.bot.resolvers.controllers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.helpers.TelegramPhotoHelper;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.controllers.view.TelegramView;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.TelegramData;
import ru.akvine.marketspace.bot.validator.PhotoValidator;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadPhotoStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final TelegramViewManager telegramViewManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final TelegramPhotoHelper telegramPhotoHelper;
    private final TelegramIntegrationService telegramIntegrationService;
    private final PhotoValidator photoValidator;

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
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

    @Override
    public SendMessage setNextState(String chatId, ClientState nextState) {
        stateStorage.add(chatId, nextState);

        TelegramView telegramView = telegramViewManager.getEventMap().get(nextState);

        String message = telegramView.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = telegramView.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
