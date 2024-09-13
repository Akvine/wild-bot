package ru.akvine.wild.bot.telegram.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.facades.CommandResolverFacade;
import ru.akvine.wild.bot.facades.StateResolverFacade;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;
import ru.akvine.wild.bot.controllers.views.TelegramView;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.BACK_BUTTON_TEXT;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcher {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final TelegramViewFacade telegramViewFacade;

    private final StateResolverFacade stateResolverFacade;
    private final TelegramDataResolverFacade resolverFacade;

    private final CommandResolverFacade commandResolverFacade;
    private final TelegramIntegrationService telegramIntegrationService;

    public BotApiMethod<?> doDispatch(TelegramData telegramData) {
        TelegramDataResolver resolver = resolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        logger.info(
                "Received in dispatcher message = [{}] with type = [{}]",
                text, telegramData.getType()
        );

        if (StringUtils.isNotBlank(text) && text.startsWith("/")) {
            CommandResolver commandResolver = commandResolverFacade
                    .getCommandResolvers()
                    .get(Command.getByText(text));
            if (commandResolver == null) {
                return new SendMessage(chatId, "Такой команды не существует!");
            }
            return commandResolver.resolve(chatId, text);
        }

        if (!stateStorage.containsState(chatId)) {
            stateStorage.add(chatId, ClientState.MAIN_MENU);
            return formMessage(chatId, ClientState.MAIN_MENU);
        }

        if (stateStorage.containsState(chatId) && stateStorage.statesCount(chatId) > 1) {
            if (StringUtils.isNotBlank(text) && text.equals(BACK_BUTTON_TEXT)) {
                ClientState previousState = stateStorage.removeCurrentAndGetPrevious(chatId);
                telegramIntegrationService.answerCallback(telegramData);
                return formMessage(chatId, previousState);
            }
        }

        return stateResolverFacade
                .getStateResolvers()
                .get(stateStorage.getCurrent(chatId))
                .resolve(telegramData);
    }


    private SendMessage formMessage(String chatId, ClientState state) {
        TelegramView view = telegramViewFacade.getEventMap().get(state);
        String message = view.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = view.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("html");
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
