package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.dto.ClientUpdate;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class InputNewWildberriesTokenMenuStateResolver extends StateResolver {
    private final ClientService clientService;

    public InputNewWildberriesTokenMenuStateResolver(
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
        String token = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        ClientUpdate action =
                new ClientUpdate().setChatId(chatId).setBotType(botType).setTokenToUpdate(token);
        clientService.update(action);

        return setNextState(chatId, stateStorage.removeCurrentAndGetPrevious(chatId), botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU;
    }
}
