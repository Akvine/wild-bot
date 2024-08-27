package ru.akvine.wild.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class CommandResolverManager {
    private final Map<Command, CommandResolver> commandResolvers;
}
