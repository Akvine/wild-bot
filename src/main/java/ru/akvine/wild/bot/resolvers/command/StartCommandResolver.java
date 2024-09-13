package ru.akvine.wild.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.resolvers.controllers.views.TelegramView;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommandResolver implements CommandResolver {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final TelegramViewFacade telegramViewFacade;

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
        TelegramView view = telegramViewFacade.getEventMap().get(startState);
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
