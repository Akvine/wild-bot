package ru.akvine.wild.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.managers.TelegramDataResolverManager;
import ru.akvine.wild.bot.managers.TelegramViewManager;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

@Component
public class InstructionStateResolver extends StateResolver {

    @Autowired
    public InstructionStateResolver(
            TelegramViewManager viewManager,
            StateStorage<String, List<ClientState>> stateStorage,
            TelegramDataResolverManager dataResolverManager,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewManager, dataResolverManager, telegramIntegrationService);
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
