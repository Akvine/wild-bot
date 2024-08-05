package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;

@Component
@RequiredArgsConstructor
@Slf4j
public class StopCommandResolver implements CommandResolver {
    private final StateStorage<String> stateStorage;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved", getCommand());
        stateStorage.setState(chatId, ClientState.INPUT_ADVERT_ID);
        return new SendMessage(chatId, "Введите advert id кампании, которую хотите остановить и снять метрики: ");
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_STOP;
    }
}
