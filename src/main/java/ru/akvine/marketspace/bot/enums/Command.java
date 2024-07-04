package ru.akvine.marketspace.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Command {

    COMMAND_START("/start"),
    COMMAND_STATISTIC("/statistic"),
    COMMAND_REPORT("/report"),
    COMMAND_STOP("/stop"),
    COMMAND_HELP("/help");

    private final String commandName;
}
