package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.AdvertStartService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.time.LocalDateTime;
import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.CHANGE_PRICE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.KEEP_PRICE_BUTTON_TEXT;

@State
public class IsChangePriceStateResolver extends StateResolver {
    private final AdvertStartService advertStartService;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Autowired
    public IsChangePriceStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                      TelegramViewFacade viewFacade,
                                      SessionStorage<String, ClientSessionData> sessionStorage,
                                      AdvertStartService advertStartService,
                                      TelegramDataResolverFacade dataResolverFacade,
                                      TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
        this.advertStartService = advertStartService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(CHANGE_PRICE_BUTTON_TEXT)) {
            ClientSessionData sessionData = sessionStorage.get(chatId);
            sessionData.setInputNewCardPriceAndDiscount(true);
            sessionStorage.save(sessionData);
            return setNextState(chatId, ClientState.INPUT_NEW_PRICE_MENU);
        } else if (text.equals(KEEP_PRICE_BUTTON_TEXT)) {
            AdvertModel startedAdvert = advertStartService.start(chatId);
            return buildMessage(chatId, startedAdvert);
        } else {
            return new SendMessage(chatId, "Необходимо выбрать действие из меню!");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.IS_CHANGE_PRICE_MENU;
    }

    private SendMessage buildMessage(String chatId, AdvertModel startedAdvert) {
        int advertId = startedAdvert.getExternalId();
        int startCpm = startedAdvert.getCpm();
        Integer startBudgetSum = startedAdvert.getStartBudgetSum();
        LocalDateTime nextCheckDateTime = startedAdvert.getNextCheckDateTime();
        String message = String.format(
                """
                           Запущена кампания с:
                           1. Advert id = %s
                           2. Начальным CPM = %s
                           3. Начальной суммой = %s руб.
                           4. Датой следующей проверки = %s 
                        """,
                advertId, startCpm, startBudgetSum, nextCheckDateTime);

        return new SendMessage(chatId, message);
    }
}
