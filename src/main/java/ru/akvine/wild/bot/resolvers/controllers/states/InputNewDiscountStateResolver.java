package ru.akvine.wild.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

@Component
public class InputNewDiscountStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Autowired
    public InputNewDiscountStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                         TelegramViewFacade viewFacade,
                                         TelegramDataResolverFacade dataResolverFacade,
                                         SessionStorage<String, ClientSessionData> sessionStorage,
                                         TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        int newDiscount;
        try {
            newDiscount = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Необходимо ввести скидку в виде числа без %");
        }

        ClientSessionData sessionData = sessionStorage.get(chatId);
        sessionData.setNewCardDiscount(newDiscount);
        sessionStorage.save(sessionData);

        return setNextState(chatId, ClientState.ACCEPT_NEW_PRICE_MENU);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_DISCOUNT_MENU;
    }
}
