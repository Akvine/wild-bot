package ru.akvine.wild.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommandResolver implements CommandResolver {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final TelegramViewFacade telegramViewFacade;

    @Override
    public Response resolve(BotType botType, String chatId, String text) {
        logger.info("[{}] resolved", getCommand());

        if (stateStorage.containsState(chatId)) {
            stateStorage.close(chatId);
        }
        if (sessionStorage.hasSession(chatId)) {
            sessionStorage.close(chatId);
        }

        ClientState startState = ClientState.MAIN_MENU;
        stateStorage.add(chatId, startState);
        BotView view = telegramViewFacade.getEventMap().get(startState);
        String message = view.getMessage(chatId);
        InlineKeyboard keyboardMarkup = view.getKeyboard(chatId, botType);

        Response response = new Response()
                .setChatId(chatId)
                .setBotType(botType);
        if (botType == BotType.TELEGRAM) {
            SendMessage sendMessage = new SendMessage(chatId, message);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(keyboardMarkup.getTelegramKeyboard());

            return response.setTelegramResponse(sendMessage);
        }

        MaxSendMessage maxSendMessage = new MaxSendMessage()
                .setChatId(chatId)
                .setText(message);
        if (keyboardMarkup.getMaxButtons() != null) {
            maxSendMessage.setAttachments(List.of(
                    MaxKeyboardFactory.toInlineKeyboardAttachment(keyboardMarkup.getMaxButtons())));
        }

        return response.setMaxSendMessage(maxSendMessage);
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_START;
    }
}
