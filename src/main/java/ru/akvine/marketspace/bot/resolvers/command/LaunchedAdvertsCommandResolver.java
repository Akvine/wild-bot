package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.akvine.marketspace.bot.controller.LaunchedAdvertsController;
import ru.akvine.marketspace.bot.enums.Command;

@Component
@RequiredArgsConstructor
@Slf4j
public class LaunchedAdvertsCommandResolver implements CommandResolver {
    private final LaunchedAdvertsController launchedAdvertsController;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved for chat with id = {} and text = {}", getCommand(), chatId, text);
        return launchedAdvertsController.getLaunchedList(chatId);
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_LAUNCHED;
    }
}
