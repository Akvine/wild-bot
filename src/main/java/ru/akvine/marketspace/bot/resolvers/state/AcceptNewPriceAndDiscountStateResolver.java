package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.AdvertStartController;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@Component
@RequiredArgsConstructor
@Slf4j
public class AcceptNewPriceAndDiscountStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;
    private final AdvertStartController advertStartController;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());
        String text = resolver.extractText(data.getData());

        logger.info("[{}] state resolved for chat with id = {} and text = {}", getState(), chatId, text);

        if (text.equalsIgnoreCase("изменить")) {
            sessionStorage.get(chatId).setInputNewCardPriceAndDiscount(true);
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
        return ClientState.ACCEPT_NEW_PRICE_AND_DISCOUNT_STATE;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {
        stateStorage.setState(chatId, nextState);
    }
}
