package ru.akvine.wild.bot.resolvers.command;

import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.Command;

public interface CommandResolver {
    Response resolve(BotType botType, String chatId, String text);

    Command getCommand();
}
