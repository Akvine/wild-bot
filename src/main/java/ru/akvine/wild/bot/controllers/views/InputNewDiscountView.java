package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class InputNewDiscountView extends AbstractBotView {

    public InputNewDiscountView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId) {
        return "Введите новую скидку (без %): ";
    }

    @Override
    public ClientState byState() {
        return ClientState.INPUT_NEW_DISCOUNT_MENU;
    }
}
