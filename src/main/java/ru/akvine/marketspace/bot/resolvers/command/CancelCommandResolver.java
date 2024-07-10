package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;

import static ru.akvine.marketspace.bot.constants.TelegramMessageConstants.DEFAULT_MESSAGE;

@Component
@RequiredArgsConstructor
public class CancelCommandResolver implements CommandResolver {
    private final StateStorage<String> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        stateStorage.removeState(chatId);
        sessionStorage.close(chatId);
        return new SendMessage(chatId, DEFAULT_MESSAGE);
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_CANCEL;
    }
}
