package ru.akvine.wild.bot.resolvers.command;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.akvine.wild.bot.enums.Command;

public interface CommandResolver {
    BotApiMethod<?> resolve(String chatId, String text);

    Command getCommand();
}
