package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class WildberriesAccountMenuView extends AbstractBotView {
    public WildberriesAccountMenuView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        return "Выберите действие из меню";
    }

    @Override
    public ClientState byState() {
        return ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU;
    }
}
