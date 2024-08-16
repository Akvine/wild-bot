package ru.akvine.marketspace.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.*;

@Component
public class MainMenuStateResolver extends StateResolver {

    @Autowired
    public MainMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                 TelegramViewManager viewManager, TelegramDataResolverManager dataResolverManager) {
        super(stateStorage, viewManager, dataResolverManager);
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(TESTS_MENU)) {
            return setNextState(chatId, ClientState.TESTS_MENU);
        } else if (text.equals(INSTRUCTIONS_FOR_USE_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.INSTRUCTIONS_MENU);
        } else if (text.equals(ADD_SUBSCRIPTION_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.SUBSCRIBE_MENU);
        } else {
            return new SendMessage(chatId, "Выберите действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.MAIN_MENU;
    }
}
