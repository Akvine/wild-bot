package ru.akvine.wild.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.managers.TelegramDataResolverManager;
import ru.akvine.wild.bot.managers.TelegramViewManager;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

@Component
public class ChooseCategoryStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Autowired
    public ChooseCategoryStateResolver(TelegramDataResolverManager dataResolverManager,
                                       TelegramViewManager viewManager,
                                       StateStorage<String, List<ClientState>> stateStorage,
                                       SessionStorage<String, ClientSessionData> sessionStorage,
                                       TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewManager, dataResolverManager, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        int categoryId;
        try {
            categoryId = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Необходимо выбрать категорию из списка!");
        }

        ClientSessionData sessionData = sessionStorage.get(chatId);
        sessionData.setSelectedCategoryId(categoryId);
        sessionStorage.save(sessionData);
        return setNextState(chatId, ClientState.UPLOAD_PHOTO_MENU);
    }

    @Override
    public ClientState getState() {
        return ClientState.CHOOSE_CATEGORY_MENU;
    }
}
