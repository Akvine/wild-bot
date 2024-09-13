package ru.akvine.wild.bot.resolvers.controllers.states;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.controllers.views.TelegramView;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class StateResolver {
    protected final StateStorage<String, List<ClientState>> stateStorage;
    protected final TelegramViewFacade viewFacade;
    protected final TelegramDataResolverFacade dataResolverFacade;
    private final TelegramIntegrationService telegramIntegrationService;

    @Nullable
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        telegramIntegrationService.answerCallback(telegramData);
        logger.info("[{}] state resolved", getState());
        return null;
    }

    public abstract ClientState getState();

    protected SendMessage setNextState(String chatId, ClientState nextState) {
        stateStorage.add(chatId, nextState);
        TelegramView telegramView = viewFacade.getEventMap().get(nextState);

        String message = telegramView.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = telegramView.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("html");
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
