package ru.akvine.marketspace.bot.telegram.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.CommandResolverManager;
import ru.akvine.marketspace.bot.managers.StateResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.command.CommandResolver;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.resolvers.controllers.views.TelegramView;
import ru.akvine.marketspace.bot.telegram.TelegramData;
import ru.akvine.marketspace.bot.utils.TelegramUtils;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.BACK_BUTTON_TEXT;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcher {
    private final StateStorage<String, List<ClientState>> stateStorage;
    private final TelegramViewManager telegramViewManager;

    private final StateResolverManager stateResolverManager;
    private final TelegramDataResolverManager resolverManager;

    private final CommandResolverManager commandResolverManager;

    public BotApiMethod<?> doDispatch(TelegramData telegramData) {
        TelegramDataResolver resolver = resolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        logger.info(
                "Received in dispatcher message = [{}] with type = [{}]",
                text, telegramData.getType()
        );

        if (TelegramUtils.isSticker(telegramData)) {
            return new SendMessage(chatId, "Бот не поддерживает стикеры :-(");
        }

        if (StringUtils.isNotBlank(text) && text.startsWith("/")) {
            CommandResolver commandResolver = commandResolverManager
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
                return formMessage(chatId, previousState);
            }
        }

        return stateResolverManager
                .getStateResolvers()
                .get(stateStorage.getCurrent(chatId))
                .resolve(telegramData);
    }


    private SendMessage formMessage(String chatId, ClientState state) {
        TelegramView view = telegramViewManager.getEventMap().get(state);
        String message = view.getMessage(chatId);
        InlineKeyboardMarkup keyboardMarkup = view.getKeyboard(chatId);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }
}
