package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.CHANGE_WAREHOUSE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.REVOKE_TOKEN_BUTTON_TEXT;

import java.util.List;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class WildberriesAccountSettingsMenuStateResolver extends StateResolver {
    private final ClientService clientService;

    public WildberriesAccountSettingsMenuStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            TelegramIntegrationService telegramIntegrationService,
            ClientService clientService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.clientService = clientService;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        if (REVOKE_TOKEN_BUTTON_TEXT.equals(text)) {
            clientService.revokeToken(chatId, botType);
            return setNextState(chatId, ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU, botType);
        } else if (CHANGE_WAREHOUSE_BUTTON_TEXT.equals(text)) {
            return setNextState(chatId, ClientState.CHANGE_WAREHOUSE_ID_MENU, botType);
        } else {
            // TODO: boilerplate-код. Надо в рамках отдельного коммита убрать
            Response response = new Response(chatId, botType);
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "Необходимо выбрать действие из меню!"));
            }

            return response.setText("Необходимо выбрать действие из меню!");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU;
    }
}
