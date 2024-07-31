package ru.akvine.marketspace.bot.telegram;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.enums.Command;

@Component
public class CommandResolver {

    @Nullable
    public Command resolve(String text) {
        if (Command.COMMAND_START.getCommandName().equals(text)) {
            return Command.COMMAND_START;
        }
        if (Command.COMMAND_STOP.getCommandName().equals(text)) {
            return Command.COMMAND_STOP;
        }
        if (Command.COMMAND_CANCEL.getCommandName().equals(text)) {
            return Command.COMMAND_CANCEL;
        }
        if (Command.COMMAND_STATS.getCommandName().equals(text)) {
            return Command.COMMAND_STATS;
        }
        if (Command.COMMAND_PHOTO.getCommandName().equals(text)) {
            return Command.COMMAND_PHOTO;
        }
        if (Command.COMMAND_REPORT.getCommandName().equals(text)) {
            return Command.COMMAND_REPORT;
        }
        if (Command.COMMAND_HELP.getCommandName().equals(text)) {
            return Command.COMMAND_HELP;
        }

        return null;
    }
}
