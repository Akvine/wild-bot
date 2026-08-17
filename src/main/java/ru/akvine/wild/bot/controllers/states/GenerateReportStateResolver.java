package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.START_GENERATION_BUTTON_TEXT;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.ReportService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

@State
public class GenerateReportStateResolver extends StateResolver {
    private final ReportService reportService;
    private final TelegramIntegrationService telegramIntegrationService;

    private static final String REPORT_FILENAME_WITH_EXTENSION = "report.xlsx";

    @Autowired
    public GenerateReportStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            ReportService reportService,
            TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.reportService = reportService;
        this.telegramIntegrationService = telegramIntegrationService;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        if (text.equals(START_GENERATION_BUTTON_TEXT)) {
            byte[] report = reportService.generateReport(chatId, botType);
            telegramIntegrationService.sendFile(chatId, REPORT_FILENAME_WITH_EXTENSION, report);
            return setNextState(chatId, ClientState.FINISH_GENERATION_REPORT_MENU, botType);
        } else {
            return resolveDefaultResponse(chatId, botType);
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.GENERATE_REPORT_MENU;
    }
}
