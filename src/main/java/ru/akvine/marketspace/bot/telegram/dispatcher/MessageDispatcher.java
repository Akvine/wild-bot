package ru.akvine.marketspace.bot.telegram.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.enums.TelegramDataType;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.managers.CommandResolverManager;
import ru.akvine.marketspace.bot.managers.StateResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.ClientCreate;
import ru.akvine.marketspace.bot.telegram.CommandResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import static ru.akvine.marketspace.bot.constants.TelegramMessageConstants.UNKNOWN_COMMAND_MESSAGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcher {
    private final ClientService clientService;
    private final StateStorage<String> stateStorage;

    private final StateResolverManager stateResolverManager;
    private final TelegramDataResolverManager resolverManager;

    private final CommandResolver commandResolver;
    private final CommandResolverManager commandResolverManager;

    public BotApiMethod<?> doDispatch(TelegramData telegramData) {
        TelegramDataResolver resolver = resolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        ClientBean clientBean = clientService.findByChatId(chatId);
        if ((clientBean == null || clientBean.isDeleted()) && telegramData.getType() == TelegramDataType.MESSAGE) {
            Message message = telegramData.getData().getMessage();
            ClientCreate clientCreate = new ClientCreate(
                    chatId,
                    message.getFrom().getUserName(),
                    message.getFrom().getFirstName(),
                    message.getFrom().getLastName()
            );
            clientBean = clientService.create(clientCreate);
        }

        clientService.checkIsInWhitelist(clientBean.getUsername());
        clientService.checkIsBlockedAndThrowException(clientBean.getUuid());

        logger.info("Send message = {} by client = [{}]", text, clientBean);

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
