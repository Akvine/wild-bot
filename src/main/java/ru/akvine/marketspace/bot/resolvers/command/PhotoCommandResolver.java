package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;

@Component
@Slf4j
@RequiredArgsConstructor
public class PhotoCommandResolver implements CommandResolver {
    private final StateStorage<String> stateStorage;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved for chat with id = {} and text = {}", getCommand(), chatId, text);
        stateStorage.setState(chatId, ClientState.INPUT_STATISTIC_ID);
        return new SendMessage(chatId, "Введите ID статистики, которую хотите получить из отчета: ");
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_PHOTO;
    }
}
