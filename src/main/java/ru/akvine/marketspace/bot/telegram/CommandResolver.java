package ru.akvine.marketspace.bot.telegram;

import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.enums.Command;

@Component
public class CommandResolver {
    public boolean isStartCommand(String command) {
        return Command.COMMAND_START.getCommandName().equals(command);
    }

    public boolean isStatisticCommand(String command) {
        return Command.COMMAND_STATISTIC.getCommandName().equals(command);
    }

    public boolean isReportCommand(String command) {
        return Command.COMMAND_REPORT.getCommandName().equals(command);
    }

    public boolean isCancelCommand(String command) {
        return Command.COMMAND_CANCEL.getCommandName().equals(command);
    }

    public boolean isHelpCommand(String command) {
        return Command.COMMAND_HELP.getCommandName().equals(command);
    }
}
