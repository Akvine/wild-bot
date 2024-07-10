package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.Command;

@Component
@RequiredArgsConstructor
public class HelpCommandResolver implements CommandResolver {
    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("/start - запустить кампанию в паузе или готовую к запуску \n")
                .append("/statistic - вывести статистику по запущенным кампаниям \n")
                .append("/report - генерирует отчет с тестами \n")
                .append("/help - список доступных команд \n")
                .append("/cancel - отменить запуск кампании");
        return new SendMessage(chatId, sb.toString());
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_HELP;
    }
}
