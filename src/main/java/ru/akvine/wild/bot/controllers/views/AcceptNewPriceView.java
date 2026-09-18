package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.controllers.validators.WildberriesValidator;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;

@View
public class AcceptNewPriceView extends AbstractBotView {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final WildberriesValidator wildberriesValidator;

    public AcceptNewPriceView(
            BotKeyboardFactoryFacade facade,
            SessionStorage<String, ClientSessionData> sessionStorage,
            WildberriesValidator wildberriesValidator) {
        super(facade);
        this.sessionStorage = sessionStorage;
        this.wildberriesValidator = wildberriesValidator;
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        ClientSessionData sessionData = sessionStorage.get(chatId, botType);
        return buildMessage(sessionData.getNewCardPrice(), sessionData.getNewCardDiscount());
    }

    @Override
    public ClientState byState() {
        return ClientState.ACCEPT_NEW_PRICE_MENU;
    }

    private String buildMessage(int price, int discount) {
        StringBuilder sb = new StringBuilder();
        sb.append("1. Цена без скидки: ")
                .append(price)
                .append("\n")
                .append("2. Скидка: ")
                .append(discount)
                .append("\n")
                .append("3. Цена на сайте: ")
                .append(wildberriesValidator.calculateDiscountPrice(price, discount));
        return sb.toString();
    }
}
