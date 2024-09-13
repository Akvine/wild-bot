package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.controllers.validators.StartValidator;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;

@State
public class TestsMenuStateResolver extends StateResolver {
    private final StartValidator startValidator;

    @Autowired
    public TestsMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                  TelegramViewFacade viewFacade,
                                  TelegramDataResolverFacade dataResolverFacade,
                                  StartValidator startValidator,
                                  TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.startValidator = startValidator;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(START_TEST_BUTTON_TEXT)) {
            startValidator.verifyStart(chatId);
            return setNextState(chatId, ClientState.CHOOSE_TYPE_MENU);
        } else if (text.equals(GENERATE_REPORT_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.GENERATE_REPORT_MENU);
        } else if (text.equals(LIST_STARTED_TESTS_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.LIST_STARTED_TESTS_MENU);
        } else if (text.equals(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.FILL_ADVERTISING_ACCOUNT_MENU);
        } else if (text.equals(DETAIL_TEST_INFORMATION_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.DETAIL_TEST_INFO_MENU);
        } else {
            return new SendMessage(chatId, "Выберите действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.TESTS_MENU;
    }
}
