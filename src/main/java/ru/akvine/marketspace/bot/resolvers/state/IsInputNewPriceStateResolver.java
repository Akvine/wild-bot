package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.AdvertStartController;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@Component
@RequiredArgsConstructor
@Slf4j
public class IsInputNewPriceStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;
    private final AdvertStartController advertStartController;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());
        String text = resolver.extractText(data.getData());

        logger.info("[{}] state resolved with text = {}", getState(), text);


        if (text.equalsIgnoreCase("изменить")) {
            ClientSessionData session = sessionStorage.get(chatId);
            session.setInputNewCardPriceAndDiscount(true);
            sessionStorage.save(session);

            setNextState(chatId, ClientState.INPUT_NEW_CARD_PRICE_STATE);
            return new SendMessage(chatId, "Введите новую цену для карточки: ");
        } else if (text.equalsIgnoreCase("не изменять")) {
            stateStorage.removeState(chatId);
            return advertStartController.startAdvert(chatId);
        } else {
            return new SendMessage(chatId, "Нужно выбрать \"Изменить\" или \"Не изменить\"");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.IS_NEED_INPUT_NEW_CARD_PRICE_STATE;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {
        stateStorage.setState(chatId, nextState);
    }
}
