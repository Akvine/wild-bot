package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.dto.ClientUpdate;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.property.PropertyCodes;
import ru.akvine.wild.bot.services.property.PropertyService;

@State
public class InputNewWildberriesTokenMenuStateResolver extends StateResolver {
    private final ClientService clientService;

    private final BotIntegrationAdapter botIntegrationAdapter;

    public InputNewWildberriesTokenMenuStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            TelegramIntegrationService telegramIntegrationService,
            ClientService clientService,
            BotIntegrationAdapter botIntegrationAdapter,
            PropertyService propertyService) {
        super(stateStorage, viewFacade, telegramIntegrationService, propertyService);
        this.clientService = clientService;
        this.botIntegrationAdapter = botIntegrationAdapter;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String token = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        ClientUpdate action = new ClientUpdate().setChatId(chatId).setBotType(botType);
        boolean apiTokenValidateEnabled = propertyService.getAs(
                PropertyCodes.WildberriesIntegrationPropertiesCodes.WILDBERRIES_API_TOKEN_VALIDATE_ENABLED.getName(),
                Boolean.class);
        String apiTokenPattern = propertyService.get(
                PropertyCodes.WildberriesIntegrationPropertiesCodes.WILDBERRIES_API_TOKEN_VALIDATE_PATTERN.getName());
        if (apiTokenValidateEnabled && StringUtils.isNotBlank(apiTokenPattern)) {
            if (token.matches(apiTokenPattern)) {
                action.setTokenToUpdate(token);
            } else {
                return resolveDefaultResponse(chatId, botType, "Не похоже на токен. Попробуйте еще раз!");
            }
        } else {
            action.setTokenToUpdate(token);
        }

        clientService.update(action);
        botIntegrationAdapter.sendMessage(chatId, botType, "Токен успешно обновлен!");
        return setNextState(chatId, stateStorage.removeCurrentAndGetPrevious(chatId, botType), botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU;
    }
}
