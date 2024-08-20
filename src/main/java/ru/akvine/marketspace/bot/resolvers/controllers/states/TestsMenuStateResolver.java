package ru.akvine.marketspace.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.controllers.validators.StartValidator;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.*;

@Component
public class TestsMenuStateResolver extends StateResolver {
    private final StartValidator startValidator;

    @Autowired
    public TestsMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                  TelegramViewManager viewManager,
                                  TelegramDataResolverManager dataResolverManager,
                                  StartValidator startValidator) {
        super(stateStorage, viewManager, dataResolverManager);
        this.startValidator = startValidator;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
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
