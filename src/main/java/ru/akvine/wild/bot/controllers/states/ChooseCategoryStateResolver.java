package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class ChooseCategoryStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Autowired
    public ChooseCategoryStateResolver(
            BotViewFacade viewFacade,
            StateStorage<String, List<ClientState>> stateStorage,
            SessionStorage<String, ClientSessionData> sessionStorage,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        int categoryId;
        try {
            categoryId = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return resolveDefaultResponse(chatId, botType);
        }

        ClientSessionData sessionData = sessionStorage.get(chatId);
        sessionData.setSelectedCategoryId(categoryId);
        sessionStorage.save(sessionData);
        return setNextState(chatId, ClientState.UPLOAD_PHOTO_MENU, botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.CHOOSE_CATEGORY_MENU;
    }
}
