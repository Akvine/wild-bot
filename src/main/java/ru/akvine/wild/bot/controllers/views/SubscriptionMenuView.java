package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class SubscriptionMenuView extends AbstractBotView {
    private static final String NEW_LINE = "\n";

    public SubscriptionMenuView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Оформить подписку в два клика.")
                .append(NEW_LINE)
                .append("Стоимость месячной подписки составляет 4900 рублей");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
