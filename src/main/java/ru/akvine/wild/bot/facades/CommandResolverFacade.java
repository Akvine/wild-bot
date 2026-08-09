package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.Command;
import ru.akvine.wild.bot.resolvers.command.CommandResolver;

@Getter
@AllArgsConstructor
public class CommandResolverFacade {
    private final Map<Command, CommandResolver> commandResolvers;
}
