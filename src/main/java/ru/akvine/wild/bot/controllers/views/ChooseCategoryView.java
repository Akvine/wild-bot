package ru.akvine.wild.bot.controllers.views;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.CHOOSE_CATEGORY_TEXT;

import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class ChooseCategoryView extends AbstractBotView {

    public ChooseCategoryView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId) {
        return CHOOSE_CATEGORY_TEXT;
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_CATEGORY_MENU;
    }
}
