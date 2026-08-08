package ru.akvine.wild.bot.controllers.states;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.views.BotView;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class StateResolver {
    protected final StateStorage<String, List<ClientState>> stateStorage;
    protected final TelegramViewFacade viewFacade;
    private final TelegramIntegrationService telegramIntegrationService;

    @Nullable
    public Response resolve(Payload payload) {
        if (payload.getBotType() == BotType.TELEGRAM) {
            telegramIntegrationService.answerCallback(payload.getBotDataType(), payload.getTelegramCallbackQueryId());
        }

        logger.info("[{}] state resolved", getState());
        return null;
    }

    public abstract ClientState getState();

    protected Response setNextState(String chatId, ClientState nextState, BotType botType) {
        stateStorage.add(chatId, nextState);
        BotView botView = viewFacade.getEventMap().get(nextState);

        String message = botView.getMessage(chatId);
        InlineKeyboard keyboard = botView.getKeyboard(chatId, botType);

        Response response = new Response(chatId, botType);
        if (botType == BotType.TELEGRAM) {
            SendMessage sendMessage = new SendMessage(chatId, message);
            sendMessage.enableMarkdown(true);
            sendMessage.setParseMode("html");
            sendMessage.setReplyMarkup(keyboard.getTelegramKeyboard());
            response.setTelegramResponse(sendMessage);
        } else {
            MaxSendMessage maxSendMessage = new MaxSendMessage()
                    .setChatId(chatId)
                    .setText(message);
            if (keyboard.getMaxButtons() != null) {
                maxSendMessage.setAttachments(List.of(MaxKeyboardFactory.toInlineKeyboardAttachment(keyboard.getMaxButtons())));
            }
            response.setMaxSendMessage(maxSendMessage);
        }

        return response;
    }
}
