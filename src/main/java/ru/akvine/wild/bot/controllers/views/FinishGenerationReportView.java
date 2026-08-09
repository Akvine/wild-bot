package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class FinishGenerationReportView extends AbstractBotView {

    public FinishGenerationReportView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId) {
        return "Ваш отчёт готов, вы можете перейти по команде назад для запуска ещё одного теста";
    }

    @Override
    public ClientState byState() {
        return ClientState.FINISH_GENERATION_REPORT_MENU;
    }
}
