package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class ChooseTypeView extends AbstractBotView {
    private static final String NEW_LINE = "\n";

    public ChooseTypeView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Запуск рекламной кампании \uD83D\uDE80:")
                .append(NEW_LINE)
                .append("Выберите в какой категории будет тестироваться товар:");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
