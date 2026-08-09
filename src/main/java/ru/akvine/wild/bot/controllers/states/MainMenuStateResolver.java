package ru.akvine.wild.bot.controllers.states;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.exceptions.HasNoSubscriptionException;
import ru.akvine.wild.bot.exceptions.SubscriptionExpiredException;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.SubscriptionService;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;

@State
@Slf4j
public class MainMenuStateResolver extends StateResolver {
    private final SubscriptionService subscriptionService;

    @Autowired
    public MainMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                 BotViewFacade viewFacade,
                                 SubscriptionService subscriptionService,
                                 TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.subscriptionService = subscriptionService;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        if (text.equals(TESTS_MENU)) {
            checkSubscription(chatId);
            return setNextState(chatId, ClientState.TESTS_MENU, botType);
        } else if (text.equals(INSTRUCTIONS_FOR_USE_BUTTON_TEXT)) {
            checkSubscription(chatId);
            return setNextState(chatId, ClientState.INSTRUCTIONS_MENU, botType);
        } else if (text.equals(ADD_SUBSCRIPTION_BUTTON_TEXT)) {
            return setNextState(chatId, ClientState.SUBSCRIBE_MENU, botType);
        } else {

            Response response = new Response(chatId, botType);
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(
                        new SendMessage(chatId, "Необходимо выбрать действие из меню!")
                );
            }

            return response.setText("Необходимо выбрать действие из меню!");
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
