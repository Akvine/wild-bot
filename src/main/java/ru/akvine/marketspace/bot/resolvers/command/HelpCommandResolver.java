package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.Command;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelpCommandResolver implements CommandResolver {
    private final static String NEW_LINE = "\n";
    @Value("${telegram.bot.support.url}")
    private String supportUsername;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved", getCommand());

        StringBuilder sb = new StringBuilder();
        sb
                .append("/start - начать работу с ботом").append(NEW_LINE)
                .append("/help - вывести список доступных команд").append(NEW_LINE)
                .append("За доп. информацией обратитесь пожалуйста в поддержку: ").append(supportUsername);
        return new SendMessage(chatId, sb.toString());
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_HELP;
    }
}
