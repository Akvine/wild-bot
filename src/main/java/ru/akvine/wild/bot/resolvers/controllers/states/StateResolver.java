package ru.akvine.marketspace.bot.resolvers.controllers.states;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.controllers.views.TelegramView;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class StateResolver {
    protected final StateStorage<String, List<ClientState>> stateStorage;
    protected final TelegramViewManager viewManager;
    protected final TelegramDataResolverManager dataResolverManager;
    private final TelegramIntegrationService telegramIntegrationService;

    public BotApiMethod<?> resolve(TelegramData telegramData) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        telegramIntegrationService.answerCallback(telegramData);
        logger.info("[{}] state resolved", getState());
        return new SendMessage(chatId, "Method used only for logging");
    }

    public abstract ClientState getState();

    protected SendMessage setNextState(String chatId, ClientState nextState) {
        stateStorage.add(chatId, nextState);
        TelegramView telegramView = viewManager.getEventMap().get(nextState);

        String message = telegramView.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = telegramView.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("html");
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
