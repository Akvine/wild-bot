package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.MALE_BUTTON_TEXT;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.CardTypeService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class ChooseTypeStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final CardTypeService cardTypeService;

    @Autowired
    public ChooseTypeStateResolver(
            BotViewFacade viewFacade,
            StateStorage<String, List<ClientState>> stateStorage,
            SessionStorage<String, ClientSessionData> sessionStorage,
            CardTypeService cardTypeService,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
        this.cardTypeService = cardTypeService;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        if (text.equals(MALE_BUTTON_TEXT)) {
            String cardType = cardTypeService.verifyExistsByType(text).getType();
            sessionStorage.init(chatId);
            ClientSessionData sessionData = sessionStorage.get(chatId);
            sessionData.setSelectedCardType(cardType);
            sessionStorage.save(sessionData);
            return setNextState(chatId, ClientState.CHOOSE_CATEGORY_MENU, botType);
        } else if (text.equals(FEMALE_BUTTON_TEXT)) {
            String cardType = cardTypeService.verifyExistsByType(text).getType();
            sessionStorage.init(chatId);
            ClientSessionData sessionData = sessionStorage.get(chatId);
            sessionData.setSelectedCardType(cardType);
            sessionStorage.save(sessionData);
            return setNextState(chatId, ClientState.CHOOSE_CATEGORY_MENU, botType);
        } else {
            Response response = new Response(chatId, botType);
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "Необходимо выбрать действие из меню!"));
            }

            return response.setText("Необходимо выбрать действие из меню!");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
