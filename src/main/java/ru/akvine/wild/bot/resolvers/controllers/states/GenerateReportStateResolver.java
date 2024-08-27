package ru.akvine.marketspace.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.ReportService;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.START_GENERATION_BUTTON_TEXT;

@Component
public class GenerateReportStateResolver extends StateResolver {
    private final ReportService reportService;
    private final TelegramIntegrationService telegramIntegrationService;

    private final static String REPORT_FILENAME_WITH_EXTENSION = "report.xlsx";

    @Autowired
    public GenerateReportStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                       TelegramViewManager viewManager,
                                       TelegramDataResolverManager dataResolverManager,
                                       ReportService reportService,
                                       TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewManager, dataResolverManager, telegramIntegrationService);
        this.reportService = reportService;
        this.telegramIntegrationService = telegramIntegrationService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(START_GENERATION_BUTTON_TEXT)) {
            byte[] report = reportService.generateReport(chatId);
            telegramIntegrationService.sendFile(chatId, REPORT_FILENAME_WITH_EXTENSION, report);
            return setNextState(chatId, ClientState.FINISH_GENERATION_REPORT_MENU);
        } else {
            return new SendMessage(chatId, "Вывберите действие из главного меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.GENERATE_REPORT_MENU;
    }
}