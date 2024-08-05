package ru.akvine.marketspace.bot.telegram.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.CommandResolverManager;
import ru.akvine.marketspace.bot.managers.StateResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.CommandResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import static ru.akvine.marketspace.bot.constants.TelegramMessageErrorConstants.UNKNOWN_COMMAND_MESSAGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcher {
    private final StateStorage<String> stateStorage;

    private final StateResolverManager stateResolverManager;
    private final TelegramDataResolverManager resolverManager;

    private final CommandResolver commandResolver;
    private final CommandResolverManager commandResolverManager;

    public BotApiMethod<?> doDispatch(TelegramData telegramData) {
        TelegramDataResolver resolver = resolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        logger.info(
                "Received in dispatcher message = [{}] with type = [{}]",
                text, telegramData.getType()
        );

        if (stateStorage.containsState(chatId)) {
            if (commandResolver.resolve(text) == Command.COMMAND_CANCEL) {
                return commandResolverManager
                        .getCommandResolvers()
                        .get(Command.COMMAND_CANCEL)
                        .resolve(chatId, text);
            }

            ClientState state = stateStorage.getState(chatId);
            return stateResolverManager.getStateResolvers().get(state).resolve(telegramData);
        }

        if (commandResolver.resolve(text) != null) {
            return commandResolverManager
                    .getCommandResolvers()
                    .get(commandResolver.resolve(text))
                    .resolve(chatId, text);
        } else {
            return new SendMessage(
                    chatId,
                    UNKNOWN_COMMAND_MESSAGE
            );
        }
    }
}
