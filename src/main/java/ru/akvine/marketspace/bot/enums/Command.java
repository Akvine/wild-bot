package ru.akvine.marketspace.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Command {

    COMMAND_START("/start"),
    COMMAND_LAUNCHED("/launched"),
    COMMAND_REPORT("/report"),
    COMMAND_CANCEL("/cancel"),
    COMMAND_STATISTIC("/statistic"),
    COMMAND_HELP("/help");

    private final String commandName;
}
