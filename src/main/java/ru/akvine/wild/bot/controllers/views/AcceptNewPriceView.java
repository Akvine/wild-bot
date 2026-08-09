package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.utils.WildberriesUtils;

@View
public class AcceptNewPriceView extends AbstractBotView {
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    public AcceptNewPriceView(BotKeyboardFactoryFacade facade,
                              SessionStorage<String, ClientSessionData> sessionStorage) {
        super(facade);
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String getMessage(String chatId) {
        ClientSessionData sessionData = sessionStorage.get(chatId);
        return buildMessage(sessionData.getNewCardPrice(), sessionData.getNewCardDiscount());
    }

    @Override
    public ClientState byState() {
        return ClientState.ACCEPT_NEW_PRICE_MENU;
    }

    private String buildMessage(int price, int discount) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("1. Цена без скидки: ").append(price).append("\n")
                .append("2. Скидка: ").append(discount).append("\n")
                .append("3. Цена на сайте: ").append(WildberriesUtils.calculateDiscountPrice(price, discount));
        return sb.toString();
    }
}
