package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

import java.util.List;

@State
public class InputNewPriceStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Autowired
    public InputNewPriceStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                      TelegramViewFacade viewFacade,
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

        int newPrice;
        try {
            newPrice = Integer.parseInt(text);
        } catch (NumberFormatException exception) {

            Response response = new Response(chatId, botType);
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "Необходимо ввести цену в виде числа!"));
            }

            return implement_this;
        }

        ClientSessionData sessionData = sessionStorage.get(chatId);
        sessionData.setNewCardPrice(newPrice);
        sessionStorage.save(sessionData);

        return setNextState(chatId, ClientState.INPUT_NEW_DISCOUNT_MENU, botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_PRICE_MENU;
    }
}
