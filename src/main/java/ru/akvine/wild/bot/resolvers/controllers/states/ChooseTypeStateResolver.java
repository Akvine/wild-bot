package ru.akvine.marketspace.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.CardTypeService;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.MALE_BUTTON_TEXT;

@Component
public class ChooseTypeStateResolver extends StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final CardTypeService cardTypeService;

    @Autowired
    public ChooseTypeStateResolver(TelegramDataResolverManager dataResolverManager,
                                   TelegramViewManager viewManager,
                                   StateStorage<String, List<ClientState>> stateStorage,
                                   SessionStorage<String, ClientSessionData> sessionStorage,
                                   CardTypeService cardTypeService,
                                   TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewManager, dataResolverManager, telegramIntegrationService);
        this.sessionStorage = sessionStorage;
        this.cardTypeService = cardTypeService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
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
