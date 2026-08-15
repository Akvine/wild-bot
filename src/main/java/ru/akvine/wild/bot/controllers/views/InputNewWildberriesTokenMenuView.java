package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class InputNewWildberriesTokenMenuView extends AbstractBotView {
    public InputNewWildberriesTokenMenuView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        return "Введите новый токен: ";
    }

    @Override
    public ClientState byState() {
        return ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU;
    }
}
