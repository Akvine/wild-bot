package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.dto.ClientUpdate;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class InputNewWildberriesTokenMenuStateResolver extends StateResolver {
    private final ClientService clientService;

    private final MaxIntegrationService maxIntegrationService;

    private final boolean apiTokenValidateEnabled;
    private final String apiTokenPattern;

    public InputNewWildberriesTokenMenuStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            TelegramIntegrationService telegramIntegrationService,
            ClientService clientService,
            MaxIntegrationService maxIntegrationService,
            @Value("${wildberries.api.token.validate.enabled}") boolean apiTokenValidateEnable,
            @Value("${wildberries.api.token.validate.pattern}") String apiTokenPattern) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.clientService = clientService;
        this.maxIntegrationService = maxIntegrationService;

        this.apiTokenValidateEnabled = apiTokenValidateEnable;
        this.apiTokenPattern = apiTokenPattern;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String token = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        ClientUpdate action = new ClientUpdate().setChatId(chatId).setBotType(botType);
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
        if (botType == BotType.TELEGRAM) {
            telegramIntegrationService.sendMessage(chatId, "Токен успешно обновлен!");
        } else {
            maxIntegrationService.sendMessage(chatId, new SendMessageRequest().setText("Токен успешно обновлен!"));
        }
        return setNextState(chatId, stateStorage.removeCurrentAndGetPrevious(chatId), botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU;
    }
}
