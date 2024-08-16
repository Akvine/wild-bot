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

@Component
public class InstructionStateResolver extends StateResolver {

    @Autowired
    public InstructionStateResolver(
            TelegramViewManager viewManager,
            StateStorage<String, List<ClientState>> stateStorage,
            TelegramDataResolverManager dataResolverManager) {
        super(stateStorage, viewManager, dataResolverManager);
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        return new SendMessage(chatId, "Выберите действие из меню");
    }

    @Override
    public ClientState getState() {
        return ClientState.INSTRUCTIONS_MENU;
    }
}
