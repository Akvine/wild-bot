package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.AdvertStartController;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;

@Component
@RequiredArgsConstructor
public class StartCommandResolver implements CommandResolver {
    private final StateStorage<String> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final AdvertStartController advertStartController;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        SendMessage sendMessage = advertStartController.getCategories(chatId);
        stateStorage.setState(chatId, ClientState.CHOOSE_CATEGORY_STATE);
        sessionStorage.init(chatId);
        return sendMessage;
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_START;
    }
}
