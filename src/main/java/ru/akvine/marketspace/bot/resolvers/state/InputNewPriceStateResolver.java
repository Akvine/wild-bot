package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
public class InputNewPriceStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());
        String text = resolver.extractText(data.getData());

        logger.info("[{}] state resolved for chat with id = {} and text = {}", getState(), chatId, text);

        int newPrice;
        try {
            newPrice = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Введите целое число!");
        }

        if (newPrice < 0) {
            return new SendMessage(chatId, "Цена не может быть меньше 0");
        }

        sessionStorage.get(chatId).setNewCardPrice(newPrice);
        setNextState(chatId, ClientState.INPUT_NEW_CARD_DISCOUNT_STATE);
        return new SendMessage(chatId, "Введите скидку: ");
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_CARD_PRICE_STATE;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {
        stateStorage.setState(chatId, nextState);
    }
}
