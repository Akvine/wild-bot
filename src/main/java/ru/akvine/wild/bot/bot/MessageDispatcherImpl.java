package ru.akvine.wild.bot.bot;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.BACK_BUTTON_TEXT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.facades.CommandResolverFacade;
import ru.akvine.wild.bot.facades.StateResolverFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcherImpl implements MessageDispatcher {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final BotViewFacade botViewFacade;

    private final StateResolverFacade stateResolverFacade;

    private final CommandResolverFacade commandResolverFacade;
    private final TelegramIntegrationService telegramIntegrationService;

    public Response doDispatch(Payload payload) {
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        logger.info("Received in dispatcher message = [{}] [{}]", text, payload.getBotDataType());

        if (StringUtils.isNotBlank(text) && text.startsWith("/")) {
            CommandResolver commandResolver =
                    commandResolverFacade.getCommandResolvers().get(Command.getByText(text));
            if (commandResolver == null) {
                return new Response().setChatId(chatId).setText("Такой команды не существует!");
            }
            return commandResolver.resolve(botType, chatId, text);
        }

        if (!stateStorage.containsState(chatId)) {
            stateStorage.add(chatId, ClientState.MAIN_MENU);
            return formMessage(botType, chatId, ClientState.MAIN_MENU);
        }

        if (stateStorage.containsState(chatId) && stateStorage.statesCount(chatId) > 1) {
            if (StringUtils.isNotBlank(text) && text.equals(BACK_BUTTON_TEXT)) {
                ClientState previousState = stateStorage.removeCurrentAndGetPrevious(chatId);
                if (botType == BotType.TELEGRAM) {
                    telegramIntegrationService.answerCallback(
                            payload.getBotDataType(), payload.getTelegramCallbackQueryId());
                }

                return formMessage(botType, chatId, previousState);
            }
        }

        return stateResolverFacade
                .getStateResolvers()
                .get(stateStorage.getCurrent(chatId))
                .resolve(payload);
    }

    private Response formMessage(BotType botType, String chatId, ClientState state) {
        BotView view = botViewFacade.getEventMap().get(state);
        String message = view.getMessage(chatId, botType);
        InlineKeyboard keyboard = view.getKeyboard(chatId, botType);

        Response response = new Response(chatId, botType);
        if (botType == BotType.TELEGRAM) {
            SendMessage sendMessage = new SendMessage(chatId, message);
            sendMessage.enableMarkdown(true);
            sendMessage.setParseMode("html");
            sendMessage.setReplyMarkup(keyboard.getTelegramKeyboard());
            response.setTelegramResponse(sendMessage);
        } else {
            MaxSendMessage maxSendMessage =
                    new MaxSendMessage().setChatId(chatId).setText(message);
            if (keyboard.getMaxButtons() != null) {
                maxSendMessage.setAttachments(
                        List.of(MaxKeyboardFactory.toInlineKeyboardAttachment(keyboard.getMaxButtons())));
            }
            response.setMaxSendMessage(maxSendMessage);
        }

        return response;
    }
}
