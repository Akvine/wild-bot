package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class GenerateReportView extends AbstractBotView {

    public GenerateReportView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        return "Бот сгенерирует отчёт в формате Excel по всем  проведенным тестам";
    }

    @Override
    public ClientState byState() {
        return ClientState.GENERATE_REPORT_MENU;
    }
}
