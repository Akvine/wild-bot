package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.akvine.marketspace.bot.controller.StatisticController;
import ru.akvine.marketspace.bot.enums.Command;

@Component
@RequiredArgsConstructor
public class StatisticCommandResolver implements CommandResolver {
    private final StatisticController statisticController;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        return statisticController.getStatistic(chatId);
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_STATISTIC;
    }
}
