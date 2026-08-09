package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class InputNewPriceView extends AbstractBotView {

    public InputNewPriceView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId) {
        return "Введите новую цену карточки: ";
    }

    @Override
    public ClientState byState() {
        return ClientState.INPUT_NEW_PRICE_MENU;
    }
}
