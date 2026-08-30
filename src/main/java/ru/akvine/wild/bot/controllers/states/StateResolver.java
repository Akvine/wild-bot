package ru.akvine.wild.bot.controllers.states;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.max.MaxComponentsFactory;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@RequiredArgsConstructor
@Slf4j
public abstract class StateResolver {
    protected final StateStorage<String, List<ClientState>> stateStorage;
    protected final BotViewFacade viewFacade;
    protected final TelegramIntegrationService telegramIntegrationService;

    @Nullable
    public Response resolve(Payload payload) {
        if (payload.getBotType() == BotType.TELEGRAM) {
            telegramIntegrationService.answerCallback(payload.getBotDataType(), payload.getTelegramCallbackQueryId());
        }

        logger.info("[{}] state resolved", getState());
        return null;
    }

    public abstract ClientState getState();

    protected Response resolveDefaultResponse(String chatId, BotType botType) {
        return resolveDefaultResponse(chatId, botType, "Необходимо выбрать действие из меню!");
    }

    protected Response resolveDefaultResponse(String chatId, BotType botType, String message) {
        Response response = new Response(chatId, botType);
        if (botType == BotType.TELEGRAM) {
            return response.setTelegramResponse(new SendMessage(chatId, message));
        }

        return response.setText(message);
    }

    protected Response setNextState(String chatId, ClientState nextState, BotType botType) {
        stateStorage.add(chatId, nextState);
        BotView botView = viewFacade.getEventMap().get(nextState);

        String message = botView.getMessage(chatId, botType);
        InlineKeyboard keyboard = botView.getKeyboard(chatId, botType);

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
                        List.of(MaxComponentsFactory.toInlineKeyboardAttachment(keyboard.getMaxButtons())));
            }
            response.setMaxSendMessage(maxSendMessage);
        }

        return response;
    }
}
