package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.validators.WildberriesValidator;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.property.PropertyService;

@State
public class InputNewDiscountStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final WildberriesValidator wildberriesValidator;

    @Autowired
    public InputNewDiscountStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            SessionStorage<String, ClientSessionData> sessionStorage,
            TelegramIntegrationService telegramIntegrationService,
            PropertyService propertyService,
            WildberriesValidator wildberriesValidator) {
        super(stateStorage, viewFacade, telegramIntegrationService, propertyService);
        this.sessionStorage = sessionStorage;
        this.wildberriesValidator = wildberriesValidator;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        int newDiscount;
        try {
            newDiscount = Integer.parseInt(text);
        } catch (NumberFormatException exception) {

            Response response = new Response(chatId, botType);
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(
                        new SendMessage(chatId, "Необходимо ввести скидку в виде числа без %"));
            }

            return response.setMaxSendMessage(
                    new MaxSendMessage().setChatId(chatId).setText("Необходимо ввести скидку в виде числа без %"));
        }

        wildberriesValidator.validateDiscount(newDiscount);

        // TODO: добавить валидацию скидки через WildberriesUtils
        ClientSessionData sessionData = sessionStorage.get(chatId, botType);
        sessionData.setNewCardDiscount(newDiscount);
        sessionStorage.save(sessionData, botType);

        return setNextState(chatId, ClientState.ACCEPT_NEW_PRICE_MENU, botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_DISCOUNT_MENU;
    }
}
