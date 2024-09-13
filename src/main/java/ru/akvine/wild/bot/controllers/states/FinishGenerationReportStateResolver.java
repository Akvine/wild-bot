package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

@State
public class FinishGenerationReportStateResolver extends StateResolver {

    @Autowired
    public FinishGenerationReportStateResolver(TelegramDataResolverFacade telegramDataResolverFacade,
                                               StateStorage<String, List<ClientState>> stateStorage,
                                               TelegramViewFacade viewFacade,
                                               TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramDataResolverFacade, telegramIntegrationService);
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        return new SendMessage(chatId, "Вывберите действие из меню");
    }

    @Override
    public ClientState getState() {
        return ClientState.FINISH_GENERATION_REPORT_MENU;
    }
}
