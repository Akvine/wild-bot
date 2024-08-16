package ru.akvine.marketspace.bot.resolvers.controllers.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.controllers.view.TelegramView;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.AdvertStartService;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.time.LocalDateTime;
import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.CHANGE_PRICE_BUTTON_TEXT;
import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.KEEP_PRICE_BUTTON_TEXT;

@Component
@RequiredArgsConstructor
public class IsChangePriceStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final TelegramViewManager telegramViewManager;
    private final AdvertStartService advertStartService;
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
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

    @Override
    public SendMessage setNextState(String chatId, ClientState nextState) {
        stateStorage.add(chatId, nextState);

        TelegramView telegramView = telegramViewManager.getEventMap().get(nextState);

        String message = telegramView.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = telegramView.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
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
