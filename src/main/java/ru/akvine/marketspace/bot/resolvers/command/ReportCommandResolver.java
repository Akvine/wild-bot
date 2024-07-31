package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.services.ReportService;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportCommandResolver implements CommandResolver {
    private final TelegramIntegrationService telegramIntegrationService;
    private final ReportService reportService;

    private final static String REPORT_DEFAULT_FILE_NAME = "report.xlsx";
    private final static String DEFAULT_OUTPUT_MESSAGE = "Введите команду: ";

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved", getCommand());

        byte[] report = reportService.generateReport(chatId);
        telegramIntegrationService.sendFile(new ByteArrayInputStream(report), REPORT_DEFAULT_FILE_NAME, chatId);
        return new SendMessage(chatId, DEFAULT_OUTPUT_MESSAGE);
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_REPORT;
    }
}
