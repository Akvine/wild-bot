package ru.akvine.marketspace.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.resolvers.command.CommandResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class CommandResolverManager {
    private final Map<Command, CommandResolver> commandResolvers;
}
