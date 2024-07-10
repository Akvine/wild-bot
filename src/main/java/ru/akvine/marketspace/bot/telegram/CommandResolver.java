package ru.akvine.marketspace.bot.telegram;

import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.enums.Command;

@Component
public class CommandResolver {

    public Command resolve(String text) {
        if (Command.COMMAND_START.getCommandName().equals(text)) {
            return Command.COMMAND_START;
        }
        if (Command.COMMAND_STATISTIC.getCommandName().equals(text)) {
            return Command.COMMAND_STATISTIC;
        }
        if (Command.COMMAND_REPORT.getCommandName().equals(text)) {
            return Command.COMMAND_REPORT;
        }
        if (Command.COMMAND_CANCEL.getCommandName().equals(text)) {
            return Command.COMMAND_CANCEL;
        }
        if (Command.COMMAND_HELP.getCommandName().equals(text)) {
            return Command.COMMAND_HELP;
        }

        return null;
    }
}
