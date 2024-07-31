package ru.akvine.marketspace.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Command {

    COMMAND_START("/start"),
    COMMAND_STOP("/stop"),
    COMMAND_CANCEL("/cancel"),
    COMMAND_STATS("/stats"),
    COMMAND_PHOTO("/photo"),
    COMMAND_REPORT("/report"),
    COMMAND_HELP("/help");

    private final String commandName;
}
