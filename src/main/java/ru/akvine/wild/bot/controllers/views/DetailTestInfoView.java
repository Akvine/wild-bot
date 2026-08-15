package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class DetailTestInfoView extends AbstractBotView {

    public DetailTestInfoView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        return "Введите ID теста, чтобы получить детальную информацию по проведенному тесту: ";
    }

    @Override
    public ClientState byState() {
        return ClientState.DETAIL_TEST_INFO_MENU;
    }
}
