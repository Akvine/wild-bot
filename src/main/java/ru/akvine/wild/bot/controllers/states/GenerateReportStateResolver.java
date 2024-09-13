package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.ReportService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.START_GENERATION_BUTTON_TEXT;

@State
public class GenerateReportStateResolver extends StateResolver {
    private final ReportService reportService;
    private final TelegramIntegrationService telegramIntegrationService;

    private final static String REPORT_FILENAME_WITH_EXTENSION = "report.xlsx";

    @Autowired
    public GenerateReportStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                       TelegramViewFacade viewFacade,
                                       TelegramDataResolverFacade dataResolverFacade,
                                       ReportService reportService,
                                       TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.reportService = reportService;
        this.telegramIntegrationService = telegramIntegrationService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
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
