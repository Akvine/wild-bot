package ru.akvine.wild.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentStateCommandResolver implements CommandResolver {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final BotViewFacade botViewFacade;

    @Override
    public Response resolve(BotType botType, String chatId, String text) {
        logger.info("[{}] resolved", getCommand());

        ClientState clientCurrentState = stateStorage.getCurrent(chatId, botType);
        BotView view = botViewFacade.getEventMap().get(clientCurrentState);
        String message = view.getMessage(chatId, botType);
        InlineKeyboard keyboardMarkup = view.getKeyboard(chatId, botType);

        // TODO: есть дублирующий код как и с остальными CommandResolver
        Response response = new Response().setChatId(chatId).setBotType(botType);
        if (botType == BotType.TELEGRAM) {
            SendMessage sendMessage = new SendMessage(chatId, message);
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(keyboardMarkup.getTelegramKeyboard());

            return response.setTelegramResponse(sendMessage);
        }

        MaxSendMessage maxSendMessage = new MaxSendMessage().setChatId(chatId).setText(message);
        if (keyboardMarkup.getMaxButtons() != null) {
            maxSendMessage.setAttachments(
                    List.of(MaxComponentsFactory.toInlineKeyboardAttachment(keyboardMarkup.getMaxButtons())));
        }

        return response.setMaxSendMessage(maxSendMessage);
    }

    @Override
    public Command getCommand() {
        return Command.CURRENT_STATE;
    }
}
