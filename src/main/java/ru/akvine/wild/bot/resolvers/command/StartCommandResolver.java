package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.controllers.views.TelegramView;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommandResolver implements CommandResolver {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final TelegramViewManager telegramViewManager;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved", getCommand());

        if (stateStorage.containsState(chatId)) {
            stateStorage.close(chatId);
        }
        if (sessionStorage.hasSession(chatId)) {
            sessionStorage.close(chatId);
        }

        ClientState startState = ClientState.MAIN_MENU;
        stateStorage.add(chatId, startState);
        TelegramView view = telegramViewManager.getEventMap().get(startState);
        String message = view.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = view.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_START;
    }
}
