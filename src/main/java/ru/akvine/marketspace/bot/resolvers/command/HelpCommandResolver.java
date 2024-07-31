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
    @Value("${telegram.bot.support.username}")
    private String supportUsername;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved for chat with id = {} and text = {}", getCommand(), chatId, text);

        StringBuilder sb = new StringBuilder();
        sb
                .append("/start - запустить первую попавшуюся кампанию в паузе или готовую к запуску").append(NEW_LINE)
                .append("/stop - остановить запущенную рекламную кампанию и снять метрики").append(NEW_LINE)
                .append("/cancel - отменить действие").append(NEW_LINE)
                .append("/stats - получить количество доступных тестов и список запущенных рекламных кампаний").append(NEW_LINE)
                .append("/report - сгенерировать excel-отчет о проведенных тестах").append(NEW_LINE)
                .append("/photo - получить фото карточки и статистику для опред. записи в отчете").append(NEW_LINE)
                .append("/help - вывести список доступных команд").append(NEW_LINE)
                .append("За доп. информацией обратитесь пожалуйств в поддержку: ").append(supportUsername);
        return new SendMessage(chatId, sb.toString());
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_HELP;
    }
}
