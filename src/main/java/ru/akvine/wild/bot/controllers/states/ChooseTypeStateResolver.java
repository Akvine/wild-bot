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
import ru.akvine.wild.bot.services.CardTypeService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.MALE_BUTTON_TEXT;

@State
public class ChooseTypeStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final CardTypeService cardTypeService;

    @Autowired
    public ChooseTypeStateResolver(TelegramDataResolverFacade dataResolverFacade,
                                   TelegramViewFacade viewFacade,
                                   StateStorage<String, List<ClientState>> stateStorage,
                                   SessionStorage<String, ClientSessionData> sessionStorage,
                                   CardTypeService cardTypeService,
                                   TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
        this.cardTypeService = cardTypeService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(MALE_BUTTON_TEXT)) {
            String cardType = cardTypeService.verifyExistsByType(text).getType();
            sessionStorage.init(chatId);
            ClientSessionData sessionData = sessionStorage.get(chatId);
            sessionData.setSelectedCardType(cardType);
            sessionStorage.save(sessionData);
            return setNextState(chatId, ClientState.CHOOSE_CATEGORY_MENU);
        } else if (text.equals(FEMALE_BUTTON_TEXT)) {
            String cardType = cardTypeService.verifyExistsByType(text).getType();
            sessionStorage.init(chatId);
            ClientSessionData sessionData = sessionStorage.get(chatId);
            sessionData.setSelectedCardType(cardType);
            sessionStorage.save(sessionData);
            return setNextState(chatId, ClientState.CHOOSE_CATEGORY_MENU);
        } else {
            return new SendMessage(chatId, "Выберите действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
