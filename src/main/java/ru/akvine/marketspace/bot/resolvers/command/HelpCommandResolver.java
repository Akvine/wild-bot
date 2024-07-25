package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.Command;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelpCommandResolver implements CommandResolver {
    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved for chat with id = {} and text = {}", getCommand(), chatId, text);

        StringBuilder sb = new StringBuilder();
        sb
                .append("/start - запустить первую попавшуюся кампанию в паузе или готовую к запуску\n")
                .append("/launched - вывести статистику по запущенным кампаниям\n")
                .append("/statistic - получить фото карточки и статистику для опред. записи в отчете\n")
                .append("/report - генерирует отчет с тестами\n")
                .append("/help - список доступных команд\n")
                .append("/cancel - отменить запуск кампании");
        return new SendMessage(chatId, sb.toString());
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_HELP;
    }
}
