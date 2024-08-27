package ru.akvine.wild.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
public enum Command {

    COMMAND_START("/start"),
    COMMAND_HELP("/help");

    private final String commandName;

    @Nullable
    public static Command getByText(String text) {
        for (Command command : Command.values()) {
            if (command.getCommandName().equals(text)) {
                return command;
            }
        }

        return null;
    }
}
