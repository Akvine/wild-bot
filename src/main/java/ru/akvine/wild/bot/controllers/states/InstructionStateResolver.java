package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class InstructionStateResolver extends StateResolver {

    @Autowired
    public InstructionStateResolver(
            BotViewFacade viewFacade,
            StateStorage<String, List<ClientState>> stateStorage,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();
        return resolveDefaultResponse(chatId, botType);
    }

    @Override
    public ClientState getState() {
        return ClientState.INSTRUCTIONS_MENU;
    }
}
