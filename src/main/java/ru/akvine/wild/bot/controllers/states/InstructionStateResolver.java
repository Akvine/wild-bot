package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

import java.util.List;

@State
public class InstructionStateResolver extends StateResolver {

    @Autowired
    public InstructionStateResolver(
            TelegramViewFacade viewFacade,
            StateStorage<String, List<ClientState>> stateStorage,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();

        Response response = new Response(chatId, botType);
        if (botType == BotType.TELEGRAM) {
            return response.setTelegramResponse(new SendMessage(chatId, "Выберите действие из меню"));
        }

        return response.setText("Выберите действие из меню");
    }

    @Override
    public ClientState getState() {
        return ClientState.INSTRUCTIONS_MENU;
    }
}
