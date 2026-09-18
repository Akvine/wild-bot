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
import ru.akvine.wild.bot.services.property.PropertyService;

@State
public class FinishGenerationReportStateResolver extends StateResolver {

    @Autowired
    public FinishGenerationReportStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            TelegramIntegrationService telegramIntegrationService,
            PropertyService propertyService) {
        super(stateStorage, viewFacade, telegramIntegrationService, propertyService);
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
        return ClientState.FINISH_GENERATION_REPORT_MENU;
    }
}
