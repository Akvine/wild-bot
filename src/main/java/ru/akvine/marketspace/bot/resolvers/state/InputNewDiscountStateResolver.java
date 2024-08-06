package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;
import ru.akvine.marketspace.bot.telegram.TelegramData;
import ru.akvine.marketspace.bot.utils.MathUtils;
import ru.akvine.marketspace.bot.utils.WildberriesUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputNewDiscountStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());
        String text = resolver.extractText(data.getData());

        logger.info("[{}] state resolved with text = {}", getState(), text);

        int newDiscount;
        try {
            newDiscount = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Введите целое число! (Пример: 50)");
        }

        if (newDiscount < 0) {
            return new SendMessage(chatId, "Скидка не может быть меньше 0");
        }
        if (newDiscount > 100) {
            return new SendMessage(chatId, "Скидка не может быть больше 100");
        }

        ClientSessionData session = sessionStorage.get(chatId);
        session.setNewCardDiscount(newDiscount);
        sessionStorage.save(session);

        setNextState(chatId, ClientState.INPUT_NEW_CARD_DISCOUNT_STATE);

        String message = buildMessage(
                session.getNewCardPrice(),
                session.getNewCardDiscount()
        );

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(KeyboardFactory.getYesAndNoKeyboard());

        setNextState(chatId, ClientState.ACCEPT_NEW_PRICE_AND_DISCOUNT_STATE);
        return sendMessage;
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_NEW_CARD_DISCOUNT_STATE;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {
        stateStorage.setState(chatId, nextState);
    }

    private String buildMessage(int price, int discount) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("У карточки будет следующая информация по стоимости перед запуском теста рекламной кампании: \n")
                .append("1. Цена без скидки: ").append(price).append("\n")
                .append("2. Скидка: ").append(discount).append("\n")
                .append("3. Цена на сайте: ").append(MathUtils.round(WildberriesUtils.calculateDiscountPrice(price, discount), 2)).append("\n")
                .append("Поменять цену и скидку у карточки перед запуском теста рекламной кампании?\n " +
                        "(Введите \"Изменить\" или \"Не изменять\"");
        return sb.toString();
    }
}
