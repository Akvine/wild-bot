package ru.akvine.wild.bot.resolvers.controllers.states;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.exceptions.HasNoSubscriptionException;
import ru.akvine.wild.bot.exceptions.SubscriptionExpiredException;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.SubscriptionService;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;

@Component
@Slf4j
public class MainMenuStateResolver extends StateResolver {
    private final SubscriptionService subscriptionService;

    @Autowired
    public MainMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                 TelegramViewFacade viewFacade,
                                 TelegramDataResolverFacade dataResolverFacade,
                                 SubscriptionService subscriptionService,
                                 TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.subscriptionService = subscriptionService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(TESTS_MENU)) {
            checkSubscription(chatId);
            return setNextState(chatId, ClientState.TESTS_MENU);
        } else if (text.equals(INSTRUCTIONS_FOR_USE_BUTTON_TEXT)) {
            checkSubscription(chatId);
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

    private void checkSubscription(String chatId) {
        SubscriptionModel subscription = subscriptionService.getByChatIdOrNull(chatId);
        if (subscription == null) {
            throw new HasNoSubscriptionException("Client has no subscription");
        }
        if (subscription.isExpired()) {
            throw new SubscriptionExpiredException("Client's subscription is expired!");
        }
    }
}
