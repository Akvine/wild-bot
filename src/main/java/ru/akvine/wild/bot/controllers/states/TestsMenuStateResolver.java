package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.validators.StartValidator;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class TestsMenuStateResolver extends StateResolver {
    private final StartValidator startValidator;

    @Autowired
    public TestsMenuStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            StartValidator startValidator,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.startValidator = startValidator;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        if (text.equals(START_TEST_BUTTON_TEXT)) {
            startValidator.verifyStart(chatId);
            return setNextState(chatId, ClientState.CHOOSE_TYPE_MENU, botType);
        } else if (text.equals(GENERATE_REPORT_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.GENERATE_REPORT_MENU, botType);
        } else if (text.equals(LIST_STARTED_TESTS_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.LIST_STARTED_TESTS_MENU, botType);
        } else if (text.equals(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.FILL_ADVERTISING_ACCOUNT_MENU, botType);
        } else if (text.equals(DETAIL_TEST_INFORMATION_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.DETAIL_TEST_INFO_MENU, botType);
        } else {
            return resolveDefaultResponse(chatId, botType);
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.TESTS_MENU;
    }
}
